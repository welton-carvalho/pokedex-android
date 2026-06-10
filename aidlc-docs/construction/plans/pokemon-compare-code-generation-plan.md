# Code Generation Plan — Unit: pokemon-compare

**Single source of truth para a geração de código desta unidade.** Brownfield: modificar arquivos existentes in-place (nunca criar cópias). Código no workspace root; documentação em `aidlc-docs/`.

## Contexto da Unidade
- **Objetivo**: feature de comparação de 2 Pokémon lado a lado + modo de seleção na lista.
- **Design**: `aidlc-docs/inception/application-design/` (5 artefatos).
- **Decisões**: Q1=A (orquestra no VM), Q2=B (estado/erro por coluna), Q3=A (borda+badge), Q4=A (limpa seleção ao navegar).
- **Dependências**: reúsa `GetPokemonDetailUseCase`, `PokemonRepository`, `core:design-system`, `core:ui`.
- **Ordem**: sequencial por dependência de build.

## Rastreabilidade (Requisitos → Passos)
| Req | Descrição | Passos |
|---|---|---|
| FR-1/2/3/9 | Modo seleção na lista | 13–16 |
| FR-4 | Tela 2 colunas, todas as infos | 3,7,9 |
| FR-5 | Destaque maior por stat | 3,6,9 |
| FR-6 | Reúso getPokemonDetail (paralelo) | 8 |
| FR-7 | Estados loading/success/error por coluna | 4,5,8,9 |
| FR-8 | Rota + deep link | 2,10,11,12,16 |
| SECURITY-05 | Validação ids deep link | 11 |
| NFR-3 | Testes | 18 |

---

## Passos

- [x] **Step 1 — Scaffolding & build**
  - Criar `feature/pokemon-compare/build.gradle.kts` (plugin `pokedexlab.android.feature`; namespace `br.com.pokedex.feature.pokemoncompare`; deps: route:keys, common, design-system, domain, model, ui, data:repository; bundles compose/koin-compose/navigation3/coil; testes junit5/mockk + `core:testing`).
  - Editar `settings.gradle.kts`: `include(":feature:pokemon-compare")`.

- [x] **Step 2 — Route key** (FR-8)
  - Editar `core/route/keys/.../RouteKeys.kt`: add `@Serializable data class PokemonCompareKey(val firstId: Int, val secondId: Int) : NavKey`.

- [x] **Step 3 — Models** (FR-4, FR-5)
  - Criar `ui/model/ComparePokemonUiModel.kt`: `ComparePokemonUiModel`, `CompareStatUiModel`, `StatWinner`, `StatComparisonRow`.

- [x] **Step 4 — State / Intent / Event** (FR-7)
  - Criar `ui/state/PokemonCompareState.kt` (`CompareSide`, `SideUiState`, `PokemonCompareState`), `ui/intent/PokemonCompareIntent.kt`, `ui/event/PokemonCompareEvent.kt`.

- [x] **Step 5 — Reducer (puro)** (FR-7)
  - Criar `ui/reducer/PokemonCompareReducer.kt` (`reduce` + `loading/success/error` por lado).

- [x] **Step 6 — StatComparator (puro)** (FR-5)
  - Criar `comparison/StatComparator.kt`: `buildStatComparison(first, second): List<StatComparisonRow>` (alinha por label, calcula winner FIRST/SECOND/TIE).

- [x] **Step 7 — Mapper** (FR-4)
  - Criar `mapper/PokemonCompareUiMapper.kt`: `Pokemon.toCompareUiModel()` (espelha a lógica do detalhe: type color, height/weight, abilities, stats labels).

- [x] **Step 8 — ViewModel** (FR-6, FR-7)
  - Criar `viewmodel/PokemonCompareViewModel.kt`: carrega FIRST/SECOND em paralelo (`async`), atualiza cada `SideUiState`; `onIntent(Retry(side))` recarrega só o lado; `NavigateBack` via evento.

- [x] **Step 9 — UI (Screen + Components)** (FR-4, FR-5, FR-7)
  - Criar `ui/screen/PokemonCompareScreen.kt` (2 colunas fixas, scroll vertical, back; por coluna: loading/erro+retry/conteúdo).
  - Criar `ui/component/CompareColumn.kt` e `ui/component/StatComparisonRowItem.kt` (destaque do maior por stat; empate neutro).
  - Reúsa `PokemonTypeChip` (design-system) e `PokemonStatBar`/valor (core:ui). `data-testid`-equivalente: `Modifier.testTag` em elementos interativos (botões/retry).
  - Criar `ui/preview/PokemonComparePreview.kt`.

- [x] **Step 10 — NavEntry + DI** (FR-8)
  - Criar `navigation/PokemonCompareNavEntry.kt` (`pokemonCompareEntry(navigator)`).
  - Criar `di/PokemonCompareModule.kt` (`pokemonCompareModule` — só o ViewModel; `get()` resolve o use case do grafo).

- [x] **Step 11 — Deep link** (FR-8, SECURITY-05)
  - Editar `core/route/deeplink/.../DeepLinkRouter.kt`: rota `pokedex://compare/{id1}/{id2}` com validação (`toIntOrNull` > 0) → `PokemonCompareKey` (senão null).

- [x] **Step 12 — Route navigation** (FR-8)
  - Editar `core/route/navigation/.../AppNavDisplay.kt`: registrar `pokemonCompareEntry(navigator)`.
  - Editar `core/route/navigation/build.gradle.kts`: `implementation(project(":feature:pokemon-compare"))`.

- [x] **Step 13 — List State/Intent/Event/Reducer** (FR-1,2,3,9)
  - Editar `PokemonListState` (+`isSelectionMode`,`selectedIds`), `PokemonListIntent` (+`ToggleSelectionMode`,`ToggleSelection`,`ClickCompare`), `PokemonListEvent` (+`NavigateToCompare`,`ShowSelectionLimit`), `PokemonListReducer` (transições de seleção).

- [x] **Step 14 — List ViewModel** (FR-1,2,3,9)
  - Editar `PokemonListViewModel.onIntent`: emitir `ShowSelectionLimit` no 3º; `ClickCompare` → `NavigateToCompare(ids)` + limpar seleção (Q4=A).

- [x] **Step 15 — List UI** (FR-1,2,3; Q3)
  - Editar `PokemonListScreen`: toggle "Comparar" no header; roteia toque do card (modo seleção → `ToggleSelection`, senão `ClickPokemon`); botão "Comparar (2)" (habilitado com 2); snackbar de limite; coletar `NavigateToCompare`.
  - Editar `ui/component/PokemonCard.kt`: param `selected: Boolean` + borda/badge de check (Q3=A); `testTag`.

- [x] **Step 16 — List NavEntry** (FR-3)
  - Editar `PokemonListNavEntry.kt`: passar `onNavigateToCompare = { a,b -> navigator.navigateTo(PokemonCompareKey(a,b)) }`.

- [x] **Step 17 — App DI** 
  - Editar `PokedexLabApplication.kt`: `startKoin { modules(..., pokemonCompareModule) }`.

- [x] **Step 18 — Testes unitários** (NFR-3)
  - Criar `PokemonCompareReducerTest`, `StatComparatorTest` (winner/empate/alinhamento), `PokemonCompareUiMapperTest`.
  - Editar/criar `PokemonListReducerTest` cobrindo seleção (toggle mode, add/remove, máx 2).

- [x] **Step 19 — Documentação**
  - Criar resumo em `aidlc-docs/construction/pokemon-compare/code/code-summary.md` (arquivos criados/modificados).
  - Editar `docs/GUIDE.md`: adicionar `feature:pokemon-compare` na estrutura de módulos + deep link `pokedex://compare`.

---

## Escopo estimado
- **19 passos**; ~16–18 arquivos novos + ~8 modificações; 4 arquivos de teste.
- **Build de verificação** ocorre no estágio seguinte (Build & Test): `./gradlew assembleDebug` + `./gradlew test`.
