# Components — Feature: Comparação de Pokémon

Convenções: Clean Architecture + MVI, `docs/GUIDE.md`. Features não se conhecem; comunicação via `core:route:keys`. Novo módulo segue a estrutura padrão de feature.

## NEW — `feature:pokemon-compare`
Package raiz: `br.com.pokedex.feature.pokemoncompare`

| Componente | Tipo | Responsabilidade |
|---|---|---|
| `ui/model/ComparePokemonUiModel` | UiModel (`@Immutable`) | Projeção de UI de um Pokémon para comparação (exclusiva da feature — não importa nada de `pokemon-detail`). |
| `ui/model/CompareStatUiModel` | UiModel | `label` + `value` de um stat. |
| `ui/model/StatComparisonRow` | UiModel | Linha de comparação: `firstValue`, `secondValue`, `winner` (FIRST/SECOND/TIE). |
| `ui/state/PokemonCompareState` | State | Estado da tela: lado `first` e lado `second`, cada um independente. |
| `ui/state/SideUiState` | State (sealed) | `Loading` / `Success(pokemon)` / `Error(error)` — por coluna (suporta falha parcial, Q2=B). |
| `ui/state/CompareSide` | Enum | `FIRST` / `SECOND`. |
| `ui/intent/PokemonCompareIntent` | Intent (sealed) | `Retry(side)`, `NavigateBack`. |
| `ui/event/PokemonCompareEvent` | Event (sealed) | `NavigateBack` (efêmero). |
| `ui/reducer/PokemonCompareReducer` | Reducer (puro) | `(State, Intent) → State` + helpers `loading/success/error` por lado. |
| `comparison/StatComparator` | Lógica pura | `buildStatComparison(first, second): List<StatComparisonRow>` — calcula vencedor por stat. |
| `mapper/PokemonCompareUiMapper` | Mapper | `Pokemon.toCompareUiModel(): ComparePokemonUiModel`. |
| `viewmodel/PokemonCompareViewModel` | ViewModel | Carrega os 2 detalhes em paralelo (reuso de `GetPokemonDetailUseCase`), expõe `StateFlow`, emite eventos, retry por lado. |
| `ui/screen/PokemonCompareScreen` | Composable | Renderiza 2 colunas lado a lado (scroll vertical), destaque do maior por stat. |
| `ui/component/*` | Composables | Coluna de comparação, linha de stat comparado, etc. |
| `navigation/PokemonCompareNavEntry` | Nav entry | `pokemonCompareEntry(navigator)` registrando `entry<PokemonCompareKey>`. |
| `di/PokemonCompareModule` | Koin module | `pokemonCompareModule` — registra o ViewModel. |

## MODIFIED — `feature:pokemon-list`
| Componente | Mudança |
|---|---|
| `ui/state/PokemonListState` | + `isSelectionMode: Boolean`, `selectedIds: List<Int>` (ordem preserva seleção, máx. 2). |
| `ui/intent/PokemonListIntent` | + `ToggleSelectionMode`, `ToggleSelection(id)`, `ClickCompare`. |
| `ui/event/PokemonListEvent` | + `NavigateToCompare(firstId, secondId)`, `ShowSelectionLimit`. |
| `ui/reducer/PokemonListReducer` | Trata novos intents (ativar/desativar modo limpando seleção; add/remove até 2). |
| `viewmodel/PokemonListViewModel` | Emite `NavigateToCompare`/`ShowSelectionLimit`; limpa seleção ao navegar (Q4=A). |
| `ui/component/PokemonCard` | + estado visual de selecionado (borda + badge de check, Q3=A); no modo seleção o toque marca/desmarca. |
| `ui/screen/PokemonListScreen` | Toggle "Comparar" no header; botão "Comparar (2)"; roteia toque conforme modo; snackbar de limite; callback `onNavigateToCompare`. |
| `navigation/PokemonListNavEntry` | `pokemonListEntry` passa `onNavigateToCompare`. |

## MODIFIED — Rotas e App
| Componente | Mudança |
|---|---|
| `core:route:keys` / `RouteKeys` | + `@Serializable data class PokemonCompareKey(firstId, secondId) : NavKey`. |
| `core:route:deeplink` / `DeepLinkRouter` | + `pokedex://compare/{id1}/{id2}` → `PokemonCompareKey` com validação de ids (SECURITY-05). |
| `core:route:navigation` / `AppNavDisplay` | Registra `pokemonCompareEntry(navigator)`; build.gradle passa a depender de `feature:pokemon-compare`. |
| `:app` `PokedexLabApplication` | `startKoin` carrega `pokemonCompareModule`. |
| `settings.gradle.kts` | `include(":feature:pokemon-compare")`. |
