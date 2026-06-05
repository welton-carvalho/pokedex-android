# Proposal: Pokemon Compare

## Why

Usuários que montam times Pokémon precisam comparar stats, tipos e medidas entre dois candidatos, mas hoje só conseguem ver um de cada vez na tela de detalhe. Isso força o usuário a memorizar valores ou alternar manualmente entre telas, aumentando o esforço cognitivo e a chance de erro. Uma tela de comparação lado a lado elimina esse atrito e torna o app significativamente mais útil em cenários de decisão de time.

## What Changes

- A tela de lista ganha um **modo de seleção**: o usuário ativa o modo e toca em até 2 cards — não-selecionados ficam em alpha 40%, e ao completar 2 seleções a navegação para comparação é disparada automaticamente.
- **Regras de seleção (reducer):** (a) toque em card não-selecionado com `|selecionados| < 2` → adiciona; (b) toque em card já selecionado → remove (deseleciona); (c) toque em card não-selecionado com `|selecionados| == 2` → **ignorado** (toast/snackbar indicando que o limite foi atingido); (d) o mesmo Pokémon não pode aparecer duas vezes (`Set<Int>` garante unicidade).
- Uma nova **tela de comparação** exibe artwork oficial, nome/número, chips de tipo, altura/peso e barras de stats espelhadas para cada Pokémon lado a lado.
- Cada lado da tela de comparação tem **loading, sucesso e erro independentes**, com retry individual por lado. Não existe estado `empty` por lado — os IDs chegam sempre via NavKey e são sempre válidos do ponto de vista do carregamento.
- O estado `empty` da tela de comparação é um **invariante de tela inteira**: `CompareState.InvalidInput` (ex.: `idA == idB`), tratado defensivamente pelo ViewModel antes de disparar qualquer carregamento. Na prática não ocorre via fluxo normal, mas garante contrato explícito.
- O estado `empty` no modo seleção da lista é o estado inicial: `selectedForCompare` vazio + `isCompareMode = false`.

## Scope

### In scope

- Novo módulo `feature:pokemon-compare` com tela MVI completa
- Extensão do MVI de `feature:pokemon-list` para suportar modo seleção (máx. 2)
- `@Serializable data class PokemonCompareKey(val idA: Int, val idB: Int) : NavKey` em `core:navigation` (módulo Gradle existente — **não** criar módulo novo `core:route:keys`)
- Componente `CompareStatRow` em `core:ui` — composable que internamente reutiliza/compõe `PokemonStatBar` em layout espelhado; nenhuma lógica de barra duplicada
- Registro da nova entry no grafo de navegação do `:app`

### Out of scope

- Histórico / favoritos de comparações anteriores
- Compartilhamento da comparação
- Comparação a partir da tela de detalhe
- Filtro/ordenação baseado em stats comparados

## Impact

- Módulos Gradle afetados: `feature:pokemon-compare` (novo), `feature:pokemon-list`, `core:navigation`, `core:ui`, `:app`
- Nota: `core:navigation` é o módulo correto para NavKeys (alguns rascunhos anteriores citavam `core:route:keys` — desconsiderar)
- Specs (capabilities) afetadas: `pokemon-compare-screen` (nova), `pokemon-list-selection-mode` (nova)
- Riscos / retrocompatibilidade: extensão do `PokemonListState` é aditiva (novos campos `isCompareMode: Boolean = false`, `selectedForCompare: Set<Int> = emptySet()`) — sem quebra de contrato; navegação segue o padrão `@Serializable NavKey` existente
