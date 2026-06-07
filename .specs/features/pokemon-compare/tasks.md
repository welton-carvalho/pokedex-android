# Pokemon Compare Tasks

**Design**: `.specs/features/pokemon-compare/design.md`
**Spec**: `.specs/features/pokemon-compare/spec.md`
**Status**: Approved

**Decisões de execução** (2026-06-07):
- Testes de UI (T8/T14/T15): desvio aceito — sem teste instrumentado; **validação manual** no UAT.
- Execução **sequencial inline** (sem sub-agents), uma task por vez rodando o gate.
- **1 commit atômico por task** na branch `feat/pokemon-compare-tlc-sdd` (msg `feat(compare): T<n> ...`).
- `compose-review` rodado nos composables novos (T8, T14, T15) antes do gate final.

Gate commands (de `.specs/codebase/TESTING.md`):
- **quick**: `./gradlew :<module>:test`
- **full**: `./gradlew test`
- **build**: `./gradlew assembleDebug test`

---

## Execution Plan

### Phase 1 — Foundation (sequential)
```
T1 (módulo) ─┐
T2 (domainModule/DT-1) ─┤  independentes entre si, mas todos antes da Phase 2
T3 (PokemonCompareKey) ─┘
```

### Phase 2 — Implementação (paralelo onde marcado [P])
```
        ┌→ T4 → T5 [P]            (compare: contratos → reducer)
T1 ─────┼→ T6 [P]                 (compare: model + mapper)
        └→ T4,T5,T6 → T7 → T8     (compare: VM → screen)

T3 ─────┬→ T12 → T13 [P]          (list: contratos → reducer)
        ├→ T14 [P]                (list: card)
        └→ T12,T13,T14 → T15      (list: screen + VM emit)
```

### Phase 3 — Integração (sequential)
```
T7 → T9 (DI compare)
T8,T3,T9 → T10 (nav entry + AppNavDisplay)
T9,T2 → T11 (app wiring)
T3,T15 → T16 (list nav entry)
T10,T11,T16 → T17 (gate final)
```

---

## Task Breakdown

### T1: Criar scaffolding do módulo `feature:pokemon-compare`
**What**: `build.gradle.kts` (plugin `pokedexlab.android.feature`, deps espelhando `pokemon-detail`), `AndroidManifest.xml`, `consumer-rules.pro`; `include(":feature:pokemon-compare")` em `settings.gradle.kts`.
**Where**: `feature/pokemon-compare/` + `settings.gradle.kts`
**Depends on**: None
**Reuses**: `feature/pokemon-detail/build.gradle.kts` (estrutura)
**Tests**: none · **Gate**: build
**Done when**:
- [ ] Módulo declarado e reconhecido pelo Gradle (`./gradlew :feature:pokemon-compare:tasks` lista tasks)
- [ ] `./gradlew assembleDebug` continua verde

### T2: Criar `domainModule` em `core:domain` (DT-1)
**What**: novo `domainModule` com `factory { GetPokemonListUseCase(get()) }` e `factory { GetPokemonDetailUseCase(get()) }`; **remover** essas factories de `pokemonListModule`/`pokemonDetailModule`; adicionar `koin-core` ao `core:domain`; carregar `domainModule` no `:app`.
**Where**: `core/domain/.../di/DomainModule.kt`, `core/domain/build.gradle.kts`, `feature/*/di/*Module.kt`, `app/.../PokedexLabApplication.kt`
**Depends on**: None
**Reuses**: padrão de módulos Koin existentes
**Tests**: none · **Gate**: build
**Done when**:
- [ ] Use cases resolvidos uma única vez (sem `DefinitionOverrideException` em runtime)
- [ ] App inicia e lista/detalhe seguem funcionando
- [ ] `./gradlew assembleDebug` verde

### T3: Adicionar `PokemonCompareKey` em `core:route:keys`
**What**: `@Serializable data class PokemonCompareKey(val firstId: Int, val secondId: Int) : NavKey`.
**Where**: `core/route/keys/.../RouteKeys.kt`
**Depends on**: None
**Reuses**: `PokemonDetailKey` (padrão)
**Requirement**: COMPARE-03
**Tests**: none · **Gate**: quick (`./gradlew :core:route:keys:test`)
**Done when**:
- [ ] Key compila e é serializável
- [ ] `./gradlew :core:route:keys:assembleDebug` verde

### T4: Contratos MVI do compare (State/Intent/Event)
**What**: `CompareState`, `CompareIntent` (Retry, NavigateBack), `CompareEvent` (NavigateBack).
**Where**: `feature/pokemon-compare/.../ui/{state,intent,event}/`
**Depends on**: T1
**Reuses**: contratos do `pokemon-detail`
**Requirement**: COMPARE-07, COMPARE-10
**Tests**: none · **Gate**: quick
**Done when**:
- [ ] Classes compilam; `CompareState` `@Stable` com `isLoading/first/second/error`

### T5: `CompareReducer` + teste unitário [P]
**What**: objeto puro `loading/success/error/reduce` + `CompareReducerTest`.
**Where**: `.../ui/reducer/CompareReducer.kt`, `src/test/.../CompareReducerTest.kt`
**Depends on**: T4
**Reuses**: `PokemonDetailReducer` (forma)
**Requirement**: COMPARE-07, COMPARE-10
**Tests**: unit · **Gate**: quick
**Done when**:
- [ ] `reduce` cobre Retry (→loading) e NavigateBack (estado inalterado)
- [ ] Gate: `./gradlew :feature:pokemon-compare:test` · Test count: ≥3 passam

### T6: `CompareUiModel` + mapper + `buildStatComparison` + testes [P]
**What**: `CompareUiModel`, `CompareStatUiModel`, `StatComparisonRow`; `Pokemon.toCompareUiModel()`; `buildStatComparison(first, second)` puro; testes do mapper e da comparação (incl. empate).
**Where**: `.../ui/model/CompareUiModel.kt`, `.../mapper/CompareUiMapper.kt`, `src/test/.../CompareUiMapperTest.kt`
**Depends on**: T1
**Reuses**: `pokemonTypeColor`, `PokemonImageUrlProvider`, `statLabelMap` (duplicado — DT-2), `FakePokemonData`
**Requirement**: COMPARE-04, COMPARE-05, COMPARE-06, COMPARE-12, COMPARE-13
**Tests**: unit · **Gate**: quick
**Done when**:
- [ ] `toCompareUiModel` não inclui About/description; mapeia tipos e stats
- [ ] `buildStatComparison`: maior vence; empate ⇒ ambos `true`
- [ ] Gate: `./gradlew :feature:pokemon-compare:test` · Test count: ≥4 passam

### T7: `CompareViewModel` + teste unitário
**What**: VM que carrega os 2 Pokémon em paralelo (`async`), junta resultados, trata erro/retry; expõe `StateFlow` + `events`. Teste com `GetPokemonDetailUseCase(FakePokemonRepository)`.
**Where**: `.../viewmodel/CompareViewModel.kt`, `src/test/.../CompareViewModelTest.kt`
**Depends on**: T4, T5, T6
**Reuses**: `PokemonDetailViewModel` (padrão), `FakePokemonRepository`, `MainDispatcherRule`
**Requirement**: COMPARE-07, COMPARE-10, COMPARE-11
**Tests**: unit · **Gate**: quick
**Done when**:
- [ ] Ambos sucesso → `success`; algum erro → `error`; Retry recarrega ambos
- [ ] Gate: `./gradlew :feature:pokemon-compare:test` · Test count: ≥3 passam

### T8: `CompareScreen` (UI lado a lado + realce)
**What**: tela com header/back; 2 colunas (artwork, nome, #, type chips) + Base Stats lado a lado; realce do vencedor via `color` do `PokemonStatBar`; estados loading/error(retry).
**Where**: `.../ui/screen/CompareScreen.kt`
**Depends on**: T6, T7
**Reuses**: `PokemonStatBar`, `PokemonTypeChip`, `LoadingIndicator`, `ErrorContent`, `AsyncImage`, `koinViewModel(parametersOf)`
**Requirement**: COMPARE-04, COMPARE-05, COMPARE-06, COMPARE-07, COMPARE-12, COMPARE-13
**Tests**: none ⚠️ (ver Test Co-location — tela Compose exigiria instrumentado; sem infra no projeto) · **Gate**: build
**Done when**:
- [ ] Renderiza 2 colunas sem About/description; vencedor de cada stat destacado
- [ ] `./gradlew assembleDebug` verde

### T9: `pokemonCompareModule` (DI)
**What**: `viewModel { (a: Int, b: Int) -> CompareViewModel(get(), a, b) }`.
**Where**: `.../di/PokemonCompareModule.kt`
**Depends on**: T7
**Reuses**: `pokemonDetailModule` (padrão parametrizado)
**Tests**: none · **Gate**: quick
**Done when**:
- [ ] Módulo compila; VM resolve `GetPokemonDetailUseCase` (de `domainModule`)

### T10: `pokemonCompareEntry` + registro no `AppNavDisplay`
**What**: extensão `pokemonCompareEntry(navigator)` (`entry<PokemonCompareKey>` → `CompareScreen`); registrar em `AppNavDisplay`; adicionar `implementation(project(":feature:pokemon-compare"))` ao build de `core:route:navigation`.
**Where**: `feature/pokemon-compare/.../navigation/PokemonCompareNavEntry.kt`, `core/route/navigation/.../AppNavDisplay.kt`, `core/route/navigation/build.gradle.kts`
**Depends on**: T8, T3, T9
**Reuses**: `pokemonDetailEntry` (padrão)
**Requirement**: COMPARE-03
**Tests**: none · **Gate**: build
**Done when**:
- [ ] `PokemonCompareKey` resolve para `CompareScreen` no NavDisplay
- [ ] `./gradlew assembleDebug` verde

### T11: Carregar `pokemonCompareModule` no `:app`
**What**: adicionar `pokemonCompareModule` ao `startKoin` e `implementation(project(":feature:pokemon-compare"))` ao `:app`.
**Where**: `app/.../PokedexLabApplication.kt`, `app/build.gradle.kts`
**Depends on**: T9, T2
**Reuses**: bloco `modules(...)` existente
**Tests**: none · **Gate**: build
**Done when**:
- [ ] App injeta `CompareViewModel` sem erro de Koin
- [ ] `./gradlew assembleDebug` verde

### T12: Contratos MVI do list (alterações)
**What**: `PokemonListState` (+`isCompareMode`, +`selectedIds`); `PokemonListIntent` (+`ToggleCompareMode`, +`ToggleSelection(id)`); `PokemonListEvent` (+`NavigateToCompare(firstId, secondId)`).
**Where**: `feature/pokemon-list/.../ui/{state,intent,event}/`
**Depends on**: None
**Reuses**: contratos existentes do list
**Requirement**: COMPARE-01, COMPARE-02, COMPARE-03
**Tests**: none · **Gate**: quick
**Done when**:
- [ ] Campos/intents/event adicionados; compila

### T13: `PokemonListReducer` (modo seleção) + estender teste [P]
**What**: tratar `ToggleCompareMode` (alterna modo, limpa seleção) e `ToggleSelection` (add/remove, **máx 2**, sem duplicar); estender `PokemonListReducerTest`.
**Where**: `.../ui/reducer/PokemonListReducer.kt`, `src/test/.../PokemonListReducerTest.kt`
**Depends on**: T12
**Reuses**: `PokemonListReducerTest` existente
**Requirement**: COMPARE-01, COMPARE-02
**Tests**: unit · **Gate**: quick (`./gradlew :feature:pokemon-list:test`)
**Done when**:
- [ ] Toggle mode limpa seleção; ToggleSelection respeita máx 2 e dedupe
- [ ] Test count: ≥8 passam (4 existentes + ≥4 novos), nenhum removido

### T14: `PokemonCard` com seleção [P]
**What**: `+ isSelected: Boolean = false` com indicador visual (borda/check).
**Where**: `feature/pokemon-list/.../ui/component/PokemonCard.kt`
**Depends on**: None
**Reuses**: `PokemonCard` atual
**Requirement**: COMPARE-02
**Tests**: none ⚠️ (componente Compose; sem infra de teste instrumentado) · **Gate**: build
**Done when**:
- [ ] Card mostra estado selecionado; `./gradlew assembleDebug` verde

### T15: `PokemonListScreen` (header/modo) + emissão no VM
**What**: header com botão Comparar/Cancelar + contador `n/2`; toque em modo seleção dispara `ToggleSelection`, fora dele `ClickPokemon`; `PokemonListViewModel` emite `NavigateToCompare(a,b)` ao atingir 2 selecionados e sai do modo; novo callback `onNavigateToCompare`.
**Where**: `feature/pokemon-list/.../ui/screen/PokemonListScreen.kt`, `.../viewmodel/PokemonListViewModel.kt`
**Depends on**: T12, T13, T14
**Reuses**: `PokemonListScreen`/`ViewModel` atuais, `ErrorContent`
**Requirement**: COMPARE-01, COMPARE-02, COMPARE-03, COMPARE-08, COMPARE-09
**Tests**: none ⚠️ (tela Compose; lógica de emissão coberta indiretamente pelo reducer/manual) · **Gate**: build
**Done when**:
- [ ] Botão alterna modo; 2 seleções navegam; cancelar restaura toque→detalhe
- [ ] `./gradlew assembleDebug` verde

### T16: `pokemonListEntry` navega para compare
**What**: passar `onNavigateToCompare = { a, b -> navigator.navigateTo(PokemonCompareKey(a, b)) }`.
**Where**: `feature/pokemon-list/.../navigation/PokemonListNavEntry.kt`
**Depends on**: T3, T15
**Reuses**: `pokemonListEntry` atual
**Requirement**: COMPARE-03
**Tests**: none · **Gate**: build
**Done when**:
- [ ] Selecionar 2 na lista abre `CompareScreen`; `./gradlew assembleDebug` verde

### T17: Gate final + traceabilidade
**What**: rodar build+testes completos; conferir REQ-IDs cobertos; atualizar status no spec.
**Where**: — (verificação)
**Depends on**: T10, T11, T16
**Tests**: full · **Gate**: build
**Done when**:
- [ ] `./gradlew assembleDebug test` verde (sem testes removidos silenciosamente)
- [ ] Todos os REQ COMPARE-01..13 marcados Verified (COMPARE-14 = P3, fora do MVP)

---

## Validação pré-aprovação

### Check 1 — Granularidade
| Task | Escopo | Status |
|---|---|---|
| T1 | scaffolding 1 módulo | ✅ |
| T2 | DI use cases (coeso, multi-file mas 1 conceito) | ✅ |
| T3 | 1 key | ✅ |
| T4 | 3 contratos triviais (coeso) | ✅ |
| T5 | 1 reducer + teste | ✅ |
| T6 | model+mapper (coeso) + testes | ✅ |
| T7 | 1 VM + teste | ✅ |
| T8 | 1 tela | ✅ |
| T9 | 1 módulo DI | ✅ |
| T10 | 1 nav entry + registro | ✅ |
| T11 | wiring app | ✅ |
| T12 | 3 contratos (coeso) | ✅ |
| T13 | 1 reducer + teste | ✅ |
| T14 | 1 componente | ✅ |
| T15 | 1 tela + emissão VM (coeso) | ✅ |
| T16 | 1 nav entry | ✅ |
| T17 | gate | ✅ |

### Check 2 — Diagrama × Definição (Depends on)
| Task | Depends (corpo) | Diagrama | Status |
|---|---|---|---|
| T1/T2/T3 | None | foundation | ✅ |
| T4 | T1 | T1→T4 | ✅ |
| T5 | T4 | T4→T5 | ✅ |
| T6 | T1 | T1→T6 | ✅ |
| T7 | T4,T5,T6 | →T7 | ✅ |
| T8 | T6,T7 | T7→T8 | ✅ |
| T9 | T7 | T7→T9 | ✅ |
| T10 | T8,T3,T9 | →T10 | ✅ |
| T11 | T9,T2 | →T11 | ✅ |
| T12 | None (T3 só na nav) | list start | ✅ |
| T13 | T12 | T12→T13 | ✅ |
| T14 | None | list [P] | ✅ |
| T15 | T12,T13,T14 | →T15 | ✅ |
| T16 | T3,T15 | →T16 | ✅ |
| T17 | T10,T11,T16 | →T17 | ✅ |

`[P]` (paralelos): T5 e T6 não dependem um do outro ✅; T13 e T14 não dependem um do outro ✅.

### Check 3 — Test Co-location (vs TESTING.md)
| Task | Camada | Matriz exige | Task diz | Status |
|---|---|---|---|---|
| T5 | reducer (puro) | unit | unit | ✅ |
| T6 | mapper/UI model | unit | unit | ✅ |
| T7 | ViewModel | unit | unit | ✅ |
| T13 | reducer (puro) | unit | unit | ✅ |
| T8 | Composable screen | instrumentado | none | ⚠️ Desvio aceito* |
| T14 | Composable component | instrumentado | none | ⚠️ Desvio aceito* |
| T15 | Composable screen | instrumentado | none | ⚠️ Desvio aceito* |
| T1,T2,T3,T4,T9,T10,T11,T12,T16,T17 | build/DI/contratos/nav | none | none | ✅ |

> *Desvio: o projeto **não tem infra de teste instrumentado** (Compose UI) — lacuna já registrada em `CONCERNS.md` e no track "Saúde de testes" do ROADMAP. Adicionar harness instrumentado dentro desta feature inflaria o escopo. **Decisão do usuário necessária** (ver pergunta abaixo): (a) aceitar o desvio e validar a UI manualmente no Execute, ou (b) criar uma task extra para infra de teste de UI.

---

## Requisito × Task (cobertura)
COMPARE-01 → T12,T13,T15 · COMPARE-02 → T12,T13,T14,T15 · COMPARE-03 → T3,T12,T15,T16,T10 · COMPARE-04/05/06 → T6,T8 · COMPARE-07 → T4,T5,T7,T8 · COMPARE-08/09 → T15 · COMPARE-10/11 → T5,T7 · COMPARE-12/13 → T6,T8 · COMPARE-14 (P3) → não planejado neste ciclo.
**Cobertura:** 13/13 do MVP mapeados; COMPARE-14 fora do MVP.
