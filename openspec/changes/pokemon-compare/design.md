# Design: Pokemon Compare

## Approach

Adicionamos um novo módulo `feature:pokemon-compare` que implementa uma tela MVI independente para comparação lado a lado de dois Pokémon. A seleção dos Pokémon ocorre na tela de lista existente via extensão aditiva do seu MVI (novos campos com defaults em `PokemonListState`, novos intents e eventos). A comunicação entre features é exclusivamente via `PokemonCompareKey` em `core:navigation`. Nenhum endpoint novo é necessário — ambos os lados reutilizam `GetPokemonDetailUseCase` em paralelo via `async/await` no ViewModel. Um componente novo `CompareStatRow` é adicionado em `core:ui`, compondo o `PokemonStatBar` existente em layout espelhado. `AsyncResult<T>` é adicionado em `core:common` como tipo compartilhável.

## Module & Layer Impact

| Módulo Gradle | Arquivo | Tipo de mudança | Camada |
|---|---|---|---|
| `core:common` | `AsyncResult.kt` | Novo | domain/model |
| `core:navigation` | `RouteKeys.kt` | Modificado — adiciona `PokemonCompareKey` | navigation |
| `core:ui` | `CompareStatRow.kt` | Novo composable | ui |
| `feature:pokemon-list` | `PokemonListState.kt` | Modificado — novos campos com defaults | ui/state |
| `feature:pokemon-list` | `PokemonListIntent.kt` | Modificado — novos intents | ui/intent |
| `feature:pokemon-list` | `PokemonListEvent.kt` | Modificado — novos eventos | ui/event |
| `feature:pokemon-list` | `PokemonListReducer.kt` | Modificado — novos casos | ui/reducer |
| `feature:pokemon-list` | `PokemonListViewModel.kt` | Modificado — emite novos eventos | viewmodel |
| `feature:pokemon-list` | `PokemonListScreen.kt` | Modificado — overlay de seleção, `LifecycleResumeEffect` | ui/screen |
| `feature:pokemon-compare` | `CompareState.kt` | Novo | ui/state |
| `feature:pokemon-compare` | `CompareIntent.kt` | Novo | ui/intent |
| `feature:pokemon-compare` | `CompareViewModel.kt` | Novo | viewmodel |
| `feature:pokemon-compare` | `CompareScreen.kt` | Novo | ui/screen |
| `feature:pokemon-compare` | `CompareUiModel.kt` / `StatUiModel.kt` | Novos | ui/model |
| `feature:pokemon-compare` | `CompareMapper.kt` | Novo — `Pokemon` → `CompareUiModel` | mapper |
| `feature:pokemon-compare` | `CompareKoinModule.kt` | Novo | di |
| `:app` | `AppNavDisplay.kt` / `MainActivity` | Modificado — passa `entryProvider = getEntryProvider()` no `NavDisplay` (sem extension function separada) | navigation |
| `:app` | `AppKoinModules.kt` (ou equivalente) | Modificado — inclui `compareModule` via `includes(compareModule)` | di |

## MVI Contract

### `feature:pokemon-list` — extensões

**State (`PokemonListState` — campos adicionados, todos com defaults):**
```kotlin
val isCompareMode: Boolean = false
val selectedForCompare: Set<Int> = emptySet()
```

**Intent (`PokemonListIntent` — novos casos):**
```kotlin
data object ToggleCompareMode : PokemonListIntent
data class ToggleSelectForCompare(val id: Int) : PokemonListIntent
data object ResetCompareMode : PokemonListIntent
```

**Event (`PokemonListEvent` — novos casos):**
```kotlin
data class NavigateToCompare(val idA: Int, val idB: Int) : PokemonListEvent
data object ShowSelectionLimitReached : PokemonListEvent
```

**Reducer — transições puras (`PokemonListReducer`):**

| Intent | Pre-condition | State resultante |
|---|---|---|
| `ToggleCompareMode` | qualquer | toggle `isCompareMode`; se desativando, limpa `selectedForCompare` |
| `ToggleSelectForCompare(id)` | `!isCompareMode` | ignorado |
| `ToggleSelectForCompare(id)` | `id in selected` | remove id |
| `ToggleSelectForCompare(id)` | `id not in selected` e `size < 2` | adiciona id |
| `ToggleSelectForCompare(id)` | `id not in selected` e `size == 2` | state inalterado (ViewModel emite `ShowSelectionLimitReached`) |
| `ResetCompareMode` | qualquer | `isCompareMode = false`, `selectedForCompare = emptySet()` |

**ViewModel — emissão de eventos (não o reducer):**
- Após cada `ToggleSelectForCompare`: se `selectedForCompare.size == 2` → emite `NavigateToCompare(idA, idB)`
- Se reducer não alterou state porque `size == 2` e id fora do set → emite `ShowSelectionLimitReached`

**Screen:**
- `LifecycleResumeEffect` dispatcha `ResetCompareMode` a cada resume (limpa seleção ao retornar do compare)

---

### `feature:pokemon-compare` — novo módulo

**State (`CompareState`):**
```kotlin
data class CompareState(
    val isInvalidInput: Boolean = false,
    val pokemonA: AsyncResult<CompareUiModel> = AsyncResult.Loading,
    val pokemonB: AsyncResult<CompareUiModel> = AsyncResult.Loading,
)
```

**Intent (`CompareIntent`):**
```kotlin
// Load foi removido — o ViewModel carrega em init {} usando os params do construtor (Koin)
sealed interface CompareIntent {
    data class RetryPokemon(val side: Side) : CompareIntent
}

enum class Side { A, B }
```

**ViewModel — lógica de carregamento:**

`GetPokemonDetailUseCase` retorna `Flow<Result<Pokemon>>`. O carregamento paralelo usa dois `launch` independentes que coletam o flow via `.first()` em um `try/catch`, atualizando o state com `_state.update { }` para garantir atomicidade (sem race condition de sobrescrita):

```kotlin
// Pseudo-código do CompareViewModel.load(idA, idB)
// GetPokemonDetailUseCase retorna Flow<Pokemon> (lança em falha)
if (idA == idB) {
    _state.update { it.copy(isInvalidInput = true) }
    return
}
// Job A (cancelável independentemente para retry)
jobA = viewModelScope.launch {
    _state.update { it.copy(pokemonA = AsyncResult.Loading) }
    try {
        val pokemon: Pokemon = getPokemonDetail(idA).first()  // coleta primeira emissão; lança em falha
        val uiModel = mapper.map(pokemon)
        _state.update { it.copy(pokemonA = AsyncResult.Success(uiModel)) }
    } catch (e: Exception) {
        val error: DomainError = errorHandler.handle(e)  // converte via ErrorHandler (nunca captura direto)
        _state.update { it.copy(pokemonA = AsyncResult.Error(error)) }
    }
}
// Job B análogo para idB / pokemonB
jobB = viewModelScope.launch { /* mesmo padrão */ }

// Retry: cancela apenas o Job do lado afetado e relança
fun retry(side: Side) {
    when (side) {
        Side.A -> { jobA?.cancel(); loadSide(Side.A, idA) }
        Side.B -> { jobB?.cancel(); loadSide(Side.B, idB) }
    }
}
```

**Nenhum evento** de compare screen — a navegação de volta é gerenciada pelo backstack do Navigation3.

## Navigation

O projeto usa a integração `koin-compose-navigation3` (`org.koin.androidx.compose:koin-compose-navigation3`). Entries são registradas no Koin module de cada feature via `navigation<Key>` DSL; o `NavDisplay` usa `entryProvider = getEntryProvider()` injetado pelo Koin.

```kotlin
// core:navigation — RouteKeys.kt (ou NavKeys.kt)
@Serializable
data class PokemonCompareKey(val idA: Int, val idB: Int) : NavKey

// feature:pokemon-compare — CompareKoinModule.kt (MESCLADO ao módulo Koin abaixo)
// Veja seção Dependency Injection

// :app — MainActivity / AppNavDisplay
NavDisplay(
    backStack = navigator.backStack,
    onBack = { navigator.goBack() },
    entryProvider = getEntryProvider(),  // Koin coleta todos os navigation<Key> registrados
)
```

Nenhum `pokemonCompareEntry()` extension function separada é necessário — o Koin descobre o entry automaticamente via `navigation<PokemonCompareKey>` registrado no `compareModule`.

## Gradle Dependency Graph

```
feature:pokemon-compare
    ├── core:common            (AsyncResult, DomainError, ErrorHandler, DispatcherProvider)
    ├── core:design-system     (tema, cores, shapes, tipografia)
    ├── core:domain            (GetPokemonDetailUseCase)
    ├── core:model             (Pokemon, PokemonStat)
    ├── core:navigation        (PokemonCompareKey, NavKey)
    └── core:ui                (CompareStatRow, PokemonStatBar, PokemonImageUrlProvider)
```

`feature:pokemon-compare` NÃO depende de `feature:pokemon-list`, `feature:pokemon-detail`, `data:*` ou `core:observability`.

## Domain / Data

- **Nenhum UseCase novo.** Reutiliza `GetPokemonDetailUseCase(id: Int): Flow<Pokemon>` já existente em `core:domain`. Em caso de falha de rede ou timeout, o flow lança exceção; o ViewModel captura com `try/catch` e converte via `ErrorHandler.handle(e): DomainError` (padrão obrigatório — features nunca capturam exceções diretamente).
- **Nenhuma mudança de DTO ou repositório.**
- `CompareMapper.kt` converte `Pokemon` → `CompareUiModel`:
  - `imageUrl` via `PokemonImageUrlProvider.officialArtwork(pokemon.id)`
  - `primaryTypeColor` via `pokemonTypeColor(types.first())`
  - `stats` → lista ordenada: HP, Attack, Defense, Sp. Atk, Sp. Def, Speed com label abreviado

## Dependency Injection (Koin)

```kotlin
// feature:pokemon-compare — CompareKoinModule.kt
@OptIn(KoinExperimentalAPI::class)
val compareModule = module {
    // ViewModel no nível raiz do módulo (fora de activityRetainedScope)
    // Koin cria uma instância nova a cada vez que parametersOf é chamado
    viewModel { params ->
        CompareViewModel(
            idA = params.get(),
            idB = params.get(),
            getPokemonDetail = get(),
            errorHandler = get(),
            dispatcherProvider = get(),
        )
    }

    activityRetainedScope {
        // Navigation entry: Koin registra a composable para PokemonCompareKey
        navigation<PokemonCompareKey> { key ->
            val viewModel: CompareViewModel = koinViewModel(
                parameters = { parametersOf(key.idA, key.idB) }
            )
            CompareScreen(viewModel = viewModel)
        }
    }
}
```

`koinViewModel { parametersOf(idA, idB) }` dentro da navigation entry cria uma nova instância do ViewModel a cada navegação para compare (comportamento correto — cada entrada no backstack tem o seu próprio ViewModel com IDs distintos).

Carregado em `:app` via `includes(compareModule)` no `appModule` (mesmo padrão dos outros feature modules).

## Testing Strategy

| Alvo | Ferramenta | Cobertura obrigatória |
|---|---|---|
| `PokemonListReducer` — casos de seleção | JUnit 5 + MockK | todos os 5 casos da tabela de transições + `ResetCompareMode` |
| `CompareViewModel` | JUnit 5 + MockK + `TestDispatcherProvider` | load paralelo, `isInvalidInput`, retry por lado, erro parcial |
| `CompareMapper` | JUnit 5 | campos de `CompareUiModel` e `StatUiModel` |
| `CompareScreen` | Compose UI Test | loading state, success layout, error + retry button |

## Open Questions

1. **`AsyncResult<T>` em `core:common`** — este tipo é novo; o ADR deve registrar a decisão de colocá-lo em `core:common` vs. criar um `core:async` dedicado.
2. **`LifecycleResumeEffect` vs. `OnBackStackChange`** — o mecanismo de reset do compare mode ao retornar pode ser implementado via `LifecycleResumeEffect` (escolhido) ou via hook do backstack do Navigation3. O ADR deve justificar a escolha.
