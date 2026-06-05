# Tasks: Pokemon Compare

<!-- Cada item <= 2h. Marque [x] ao concluir. Ordem segue o grafo de dependências (de baixo p/ cima). -->

## 1. core:common — AsyncResult<T>

- [x] 1.1 Criar `AsyncResult.kt` em `core:common` com sealed interface `Loading`, `Success<T>`, `Error(DomainError)` (conforme ADR-0001)

## 2. core:navigation — PokemonCompareKey

- [x] 2.1 Adicionar `@Serializable data class PokemonCompareKey(val idA: Int, val idB: Int) : NavKey` em `RouteKeys.kt` (ou `NavKeys.kt`) dentro de `core:navigation`

## 3. core:ui — CompareStatRow

- [x] 3.1 Criar `CompareStatRow.kt` em `core:ui` — composable que recebe `label`, `valueA`, `valueB`, `colorA`, `colorB`, `maxValue` e compõe dois `PokemonStatBar` em layout espelhado (`← [bar A] [valA] LABEL [valB] [bar B →]`)
- [x] 3.2 Criar `@Preview` de `CompareStatRow` com dados fictícios

## 4. feature:pokemon-compare — Módulo novo

- [x] 4.1 Criar módulo Gradle `feature:pokemon-compare` — `build.gradle.kts` com plugin `pokedexlab.android.feature` + `pokedexlab.android.compose`, dependências: `core:common`, `core:design-system`, `core:domain`, `core:model`, `core:navigation`, `core:ui`
- [x] 4.2 Adicionar `include(":feature:pokemon-compare")` em `settings.gradle.kts`
- [x] 4.3 Criar estrutura de pacotes: `br.com.pokedex.feature.pokemoncompare.{ui.state, ui.intent, ui.screen, ui.model, ui.component, viewmodel, mapper, di}`
- [x] 4.4 Criar `CompareUiModel.kt` e `StatUiModel.kt` com os shapes definidos nos specs
- [x] 4.5 Criar `Side.kt` (`enum class Side { A, B }`) e `CompareIntent.kt` (`sealed interface CompareIntent` com apenas `RetryPokemon(side: Side)` — `Load` é removido; o ViewModel dispara o carregamento em `init {}` usando `idA`/`idB` do construtor Koin, sem necessidade de intent externo)
- [x] 4.6 Criar `CompareState.kt` (`data class CompareState(isInvalidInput, pokemonA, pokemonB)`)
- [x] 4.7 Criar `CompareMapper.kt` — converte `Pokemon` → `CompareUiModel`: artwork URL via `PokemonImageUrlProvider`, `primaryTypeColor` via `pokemonTypeColor`, stats ordenados (HP, ATK, DEF, SPA, SPD, SPE) com labels abreviados
- [x] 4.8a Criar `CompareViewModel.kt` — scaffolding + load paralelo happy-path: recebe `idA`, `idB`, `GetPokemonDetailUseCase`, `ErrorHandler`, `DispatcherProvider`; bloco `init {}` dispara carregamento imediatamente (sem `CompareIntent.Load`); trata `idA == idB` com `isInvalidInput = true`; lança dois `launch` com `.first()` + `_state.update {}` para atualizar `pokemonA`/`pokemonB` com `Success`
- [x] 4.8b Completar `CompareViewModel.kt` — retry + error handling: captura exceção via `try/catch` + `ErrorHandler.handle()` → `AsyncResult.Error`; `jobA`/`jobB` canceláveis; função `retry(side: Side)` cancela o job do lado afetado e relança a carga
- [x] 4.9a Criar `CompareScreen.kt` — scaffolding + estados de loading/error/invalidInput: estrutura `Column + verticalScroll`, renderização de `isInvalidInput` (tela inteira), `AsyncResult.Loading` (indicador por lado), `AsyncResult.Error` (mensagem + botão retry por lado que dispatcha `RetryPokemon(side)`)
- [x] 4.9b Completar `CompareScreen.kt` — success layout por lado: artwork via Coil (`PokemonImageUrlProvider`), nome + número formatado, chips de tipo com `pokemonTypeColor`, linha de altura/peso, 6× `CompareStatRow` (HP, ATK, DEF, SPA, SPD, SPE)
- [x] 4.10 Criar `CompareKoinModule.kt` — `viewModel { params -> CompareViewModel(...) }` no nível raiz + `activityRetainedScope { navigation<PokemonCompareKey> { key -> CompareScreen(...) } }`

## 5. feature:pokemon-list — Extensão do MVI

- [x] 5.1 Adicionar campos `isCompareMode: Boolean = false` e `selectedForCompare: Set<Int> = emptySet()` em `PokemonListState.kt`
- [x] 5.2 Adicionar intents `ToggleCompareMode`, `ToggleSelectForCompare(id: Int)` e `ResetCompareMode` em `PokemonListIntent.kt`
- [x] 5.3 Adicionar eventos `NavigateToCompare(idA: Int, idB: Int)` e `ShowSelectionLimitReached` em `PokemonListEvent.kt`
- [x] 5.4 Atualizar `PokemonListReducer.kt` — implementar todos os 5 casos de `ToggleSelectForCompare`: (a) `!isCompareMode` → state inalterado (guard); (b) `id in set` → remove; (c) `id not in set && size < 2` → adiciona; (d) `id not in set && size == 2` → state inalterado (ViewModel emitirá evento); (e) unicidade garantida por `Set<Int>`. Também: `ToggleCompareMode` (toggle + clear ao desativar) + `ResetCompareMode` (volta defaults)
- [x] 5.5 Atualizar `PokemonListViewModel.kt` — detectar após cada `ToggleSelectForCompare`: se `size == 2` → emitir `NavigateToCompare`; se state não mudou com id fora do set e `size == 2` → emitir `ShowSelectionLimitReached`
- [x] 5.6 Atualizar `PokemonListScreen.kt` — adicionar: overlay de seleção (alpha 40% para não-selecionados), botão toggle de compare mode, coletar `NavigateToCompare` event e chamar `navigator.navigateTo(PokemonCompareKey(idA, idB))`, coletar `ShowSelectionLimitReached` event e mostrar snackbar, `LifecycleResumeEffect` para dispatchar `ResetCompareMode`

## 6. :app — Wiring de navegação e Koin

- [x] 6.1 Adicionar dependência de `feature:pokemon-compare` em `app/build.gradle.kts`
- [x] 6.2 Incluir `compareModule` no `startKoin` do `:app` (via `includes(compareModule)` no `appModule`)
- [x] 6.3 Verificar que `NavDisplay` usa `entryProvider = getEntryProvider()` (sem extension function separada — Koin descobre automaticamente o `navigation<PokemonCompareKey>`)

## 7. Testes

- [x] 7.1 Testar `PokemonListReducer` — 5 casos de `ToggleSelectForCompare`: (a) `!isCompareMode` ignorado, (b) deselect, (c) add, (d) limit-ignored quando `size==2`, (e) unicidade por Set; + `ToggleCompareMode` (ativar, desativar + limpar seleção) + `ResetCompareMode` (JUnit 5)
- [x] 7.2 Testar `CompareViewModel` — load paralelo com sucesso em ambos os lados, `isInvalidInput` para `idA == idB`, erro em um lado + sucesso no outro, retry por lado, uso de `TestDispatcherProvider` (JUnit 5 + MockK)
- [x] 7.3 Testar `CompareMapper` — todos os campos de `CompareUiModel` e `StatUiModel`, ordenação dos stats, `primaryTypeColor` (JUnit 5)
- [x] 7.4 Testar `CompareScreen` — loading state, success layout com artwork/stats, error state + botão retry visível (Compose UI Test)

## 8. Verificação

- [x] 8.1 Rodar `/opsx:verify`
- [x] 8.2 `./gradlew assembleDebug` — build limpo sem erros
- [x] 8.3 `./gradlew test` — todos os testes passando (escopo: módulos da change; `data:repository` tem falha de compilação pré-existente fora do escopo desta change)
