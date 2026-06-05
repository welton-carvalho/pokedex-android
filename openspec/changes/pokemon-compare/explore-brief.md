# Explore Brief — pokemon-compare

**Data:** 2026-06-04  
**Objetivo:** Comparar stats de 2 Pokémon lado a lado, selecionados a partir da tela de lista.

---

## Contexto investigado

- `PokemonListUiModel` só carrega `id`, `displayName`, `imageUrl` — sem stats (paging usa o endpoint de lista)
- Stats disponíveis via `GetPokemonDetailUseCase` (já existe em `core:domain`)
- `PokemonStatBar` já existe em `core:ui` — precisará de variante espelhada para comparação
- `RouteKeys` segue padrão `NavKey` do Navigation3 — será necessário adicionar `PokemonCompareKey`
- `AppNavDisplay` registra entries por feature — novo entry de compare será adicionado lá
- `PokemonListState` hoje não tem seleção — precisará de extensão no MVI existente

---

## Decisões tomadas

| # | Decisão | Escolha |
|---|---|---|
| 1 | Estrutura do compare screen | Módulo novo `feature:pokemon-compare` |
| 2 | Selecionar o mesmo Pokémon 2× | Proibido — segundo toque deseleciona |
| 3 | Conteúdo da tela de comparação | Artwork + tipos + altura/peso + stats espelhados |
| 4 | Visual de seleção na lista | Dimming (alpha 40%) dos cards não selecionados |
| 5 | Imagem na tela de comparação | Sim — artwork oficial de cada Pokémon |
| 6 | Estado de loading | Independente por lado (pokemonA e pokemonB) |
| 7 | Retry em caso de erro | Por lado — `RetryPokemon(side: Side)` |

---

## Arquitetura acordada

### Módulos tocados

| Módulo | Tipo de mudança |
|---|---|
| `core:route:keys` | + `PokemonCompareKey(idA: Int, idB: Int) : NavKey` |
| `core:ui` | + `CompareStatRow` (barra espelhada, 2 valores) |
| `feature:pokemon-list` | Extensão do MVI — modo seleção |
| `feature:pokemon-compare` | Novo módulo completo (MVI + Koin + NavEntry) |
| `core:route:navigation` | + `pokemonCompareEntry(navigator)` no `AppNavDisplay` |

### Grafo de dependências do novo módulo

```
feature:pokemon-compare
    ├── core:common
    ├── core:design-system
    ├── core:domain          (GetPokemonDetailUseCase)
    ├── core:model           (Pokemon, PokemonStat)
    ├── core:route:keys      (PokemonCompareKey)
    └── core:ui              (CompareStatRow)
```

### Fluxo de navegação

```
PokemonListScreen (modo seleção)
    → selectedForCompare: Set<Int> (max 2)
    → intent: NavigateToCompare(idA, idB)
    → event: NavigateToCompare(idA, idB)
    → navigator.navigateTo(PokemonCompareKey(idA, idB))
    → CompareScreen busca ambos em paralelo via GetPokemonDetailUseCase
```

### Contratos MVI do compare screen

```kotlin
data class CompareState(
    val pokemonA: AsyncResult<CompareUiModel> = AsyncResult.Loading,
    val pokemonB: AsyncResult<CompareUiModel> = AsyncResult.Loading,
)

sealed interface AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val error: DomainError) : AsyncResult<Nothing>
}

sealed interface CompareIntent {
    data class Load(val idA: Int, val idB: Int) : CompareIntent
    data class RetryPokemon(val side: Side) : CompareIntent
}

enum class Side { A, B }
```

### Extensão do MVI da lista

```kotlin
// PokemonListState — campos adicionados
val isCompareMode: Boolean = false
val selectedForCompare: Set<Int> = emptySet()  // max 2

// PokemonListIntent — intents adicionados
data object ToggleCompareMode : PokemonListIntent
data class ToggleSelectForCompare(val id: Int) : PokemonListIntent

// PokemonListEvent — evento adicionado
data class NavigateToCompare(val idA: Int, val idB: Int) : PokemonListEvent
```

### Layout da tela de comparação

- Header: artwork de cada Pokémon + nome + número
- Seção Tipos: chips de tipo de cada lado
- Seção Tamanho & Peso: altura e peso lado a lado
- Seção Stats: `CompareStatRow` espelhado por stat (HP, ATK, DEF, SPA, SPD, SPE)
- Scroll vertical, conteúdo não cabe em tela fixa

### Componente `CompareStatRow` (novo em `core:ui`)

```
← [bar A] [valA]  LABEL  [valB] [bar B →]
```
Barra cresce para fora dos dois lados. Cor de cada barra = `typeColor` do respectivo Pokémon.

---

## Restrições

- **Sem novos endpoints** — reutiliza `GetPokemonDetailUseCase` (endpoint `/pokemon/{id}`)
- **Features não se conhecem** — `pokemon-compare` não importa nada de `pokemon-list` ou `pokemon-detail`
- **Mesmo Pokémon não pode ser selecionado duas vezes** — `Set<Int>` garante unicidade; segundo toque remove
