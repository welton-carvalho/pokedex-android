# ADR-0001: AsyncResult<T> em core:common e Reset do Compare Mode via LifecycleResumeEffect

## Status

Accepted

## Context

Durante o design da feature `pokemon-compare` duas decisões arquiteturais transversais emergiram:

1. **Onde declarar `AsyncResult<T>`** — O `CompareViewModel` precisa de um wrapper sealed para representar os três estados de carregamento por lado (Loading, Success, Error). Esse padrão é potencialmente reutilizável por outras features (ex.: `pokemon-detail`). A questão é se esse tipo deve viver em um módulo compartilhado ou ser feature-private.

2. **Como resetar o compare mode ao retornar da tela de comparação** — O `PokemonListState` mantém `isCompareMode` e `selectedForCompare`. Quando o usuário navega para compare e volta, a lista deve retornar ao estado normal. Há duas abordagens viáveis com o Navigation3.

Restrição transversal: features não se conhecem; `feature:pokemon-compare` não pode importar nada de `feature:pokemon-list`. O reset é responsabilidade exclusiva de `feature:pokemon-list`.

---

## Decisão 1: Onde declarar `AsyncResult<T>`

### Options Considered

#### Option A: Declarar em `core:common`

- Pros:
  - Reutilizável por qualquer feature que precise de loading/success/error (ex.: `pokemon-detail`)
  - Consistência — um único tipo no codebase, não N variantes locais
  - Alinhado com o padrão do projeto: `DomainError`, `ErrorHandler` já vivem em `core:common`
  - Evita duplicação quando a segunda feature precisar do mesmo wrapper

- Cons:
  - Adiciona um tipo ao módulo `core:common` que pode não ser usado imediatamente por outras features
  - Requer modificação de um módulo existente (vs. criar um novo)

#### Option B: Declarar em `feature:pokemon-compare` (feature-private)

- Pros:
  - Zero acoplamento novo entre módulos
  - Simples de implementar — sem toque em `core:common`

- Cons:
  - Duplicação certa quando a próxima feature precisar do mesmo wrapper
  - Viola o princípio de reuso que o projeto já pratica com `DomainError`
  - Futuro refactor inevitável (mover para `core:common`) com custo de migration

#### Option C: Criar módulo `core:async` dedicado

- Pros:
  - Separação de concerns clara
  - Grafo de dependências semântico

- Cons:
  - Overhead de um novo módulo Gradle para um único tipo
  - Complexidade de configuração desproporcional ao benefício
  - `core:common` já existe e é o lugar natural para utilities transversais de resultado

### Decision

**Option A — `AsyncResult<T>` em `core:common`.**

O padrão já estabelecido por `DomainError` e `ErrorHandler` em `core:common` justifica colocar `AsyncResult<T>` no mesmo módulo. O custo de mover depois (Option B) supera o custo de adicionar ao módulo agora (Option A). Option C é over-engineering para um único sealed interface.

Assinatura definida (os casos `Error` carregam `DomainError`, não `Exception`, para preservar a invariante do `ErrorHandler`):
```kotlin
// core:common — AsyncResult.kt
sealed interface AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val error: DomainError) : AsyncResult<Nothing>
}
```

### Consequences

- Positivas:
  - `feature:pokemon-detail` (Fase 6) pode reutilizar `AsyncResult<T>` sem duplicar código
  - Um único tipo para loading state em todas as features futuras
  - Consistência com `DomainError` / `ErrorHandler` já em `core:common`

- Negativas:
  - `core:common` cresce marginalmente (um arquivo novo)
  - Qualquer mudança de assinatura em `AsyncResult<T>` requer recompilação de todos os módulos que dependem de `core:common`

---

## Decisão 2: Reset do Compare Mode via LifecycleResumeEffect vs. backstack change hook

### Options Considered

#### Option A: `LifecycleResumeEffect` na `PokemonListScreen`

Ao voltar do compare, a `PokemonListScreen` fica visível novamente. O `LifecycleResumeEffect` é um composable que dispara um efeito a cada `ON_RESUME` do ciclo de vida do host. A screen usa isso para dispatchar `ResetCompareMode`.

```kotlin
LifecycleResumeEffect(Unit) {
    viewModel.onIntent(PokemonListIntent.ResetCompareMode)
    onPauseOrDispose { /* no-op */ }
}
```

- Pros:
  - API simples e disponível no Compose lifecycle
  - Não requer conhecimento da API interna do Navigation3 (desacoplado do navegador)
  - Funciona corretamente em qualquer cenário de retorno (back gesture, botão, deep link)
  - Testável via `TestLifecycleOwner`

- Cons:
  - Dispara em **todo** resume, inclusive ao voltar de outros destinos que não o compare (ex.: futuro splash ou dialog). Requer que `ResetCompareMode` seja idempotente (já é, pois reseta para os defaults)
  - Acoplamento implícito a lifecycle do Android (não puramente Navigation3)

#### Option B: Observer de backstack change do Navigation3

Navigation3 expõe o backstack como `SnapshotStateList`. É possível derivar um `LaunchedEffect` que observa mudanças e detecta quando `PokemonCompareKey` saiu do topo.

```kotlin
// Exemplo ilustrativo (Opção B — rejeitada)
val isOnTop by remember { derivedStateOf { backStack.lastOrNull() is PokemonListRoute } }
LaunchedEffect(isOnTop) {
    if (isOnTop) viewModel.onIntent(PokemonListIntent.ResetCompareMode)
}
```

- Pros:
  - Semântica precisa — dispara apenas quando voltamos para a lista
  - Acoplamento explícito à estrutura do backstack

- Cons:
  - Requer que a `PokemonListScreen` tenha acesso ao backstack (ou ao Navigator)
  - Viola o encapsulamento da screen — ela não deve saber de outras rotas (ex.: `PokemonListRoute` seria o NavKey da própria screen, criando acoplamento circular entre screen e NavKey host)
  - Frágil se a estrutura de backstack mudar (ex.: multi-backstack no futuro)

### Decision

**Option A — `LifecycleResumeEffect` na `PokemonListScreen`.**

A idempotência de `ResetCompareMode` elimina o único ponto negativo de Option A. Option B quebra o encapsulamento da screen e introduz dependência circular entre rota e screen. O `LifecycleResumeEffect` é a solução padrão Compose para "faça X quando esta screen ficar visível".

### Consequences

- Positivas:
  - `PokemonListScreen` não precisa acessar o backstack ou o Navigator diretamente
  - Funciona corretamente com navegação aninhada futura (multi-backstack)
  - `ResetCompareMode` sendo idempotente torna o comportamento previsível em qualquer sequência de navegação

- Negativas:
  - `ResetCompareMode` é despachado em **todo** resume da lista, mesmo quando o compare mode nunca foi ativado (overhead mínimo — apenas uma atualização de state sem mudança real se `isCompareMode == false` e `selectedForCompare.isEmpty()`)
  - Requer a dependência `androidx.lifecycle:lifecycle-runtime-compose` no módulo `feature:pokemon-list` (já provavelmente presente via Compose BOM, mas deve ser verificado)
