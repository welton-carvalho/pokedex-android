# Component Methods (Signatures) — Feature: Comparação de Pokémon

> Regras de negócio detalhadas são implementadas no Code Generation. Aqui ficam assinaturas e contratos.

## feature:pokemon-compare

### Models — `ui/model/ComparePokemonUiModel.kt`
```kotlin
@Immutable
data class ComparePokemonUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val typeColor: Color,
    val types: List<String>,
    val height: String,
    val weight: String,
    val abilities: List<String>,
    val description: String,
    val stats: List<CompareStatUiModel>,
)

@Immutable
data class CompareStatUiModel(val label: String, val value: Int)

enum class StatWinner { FIRST, SECOND, TIE }

@Immutable
data class StatComparisonRow(
    val label: String,
    val firstValue: Int,
    val secondValue: Int,
    val winner: StatWinner,
)
```

### State — `ui/state/PokemonCompareState.kt`
```kotlin
enum class CompareSide { FIRST, SECOND }

@Immutable
sealed interface SideUiState {
    data object Loading : SideUiState
    data class Success(val pokemon: ComparePokemonUiModel) : SideUiState
    data class Error(val error: DomainError) : SideUiState
}

@Stable
data class PokemonCompareState(
    val first: SideUiState = SideUiState.Loading,
    val second: SideUiState = SideUiState.Loading,
)
```

### Intent / Event
```kotlin
sealed interface PokemonCompareIntent {
    data class Retry(val side: CompareSide) : PokemonCompareIntent
    data object NavigateBack : PokemonCompareIntent
}

sealed interface PokemonCompareEvent {
    data object NavigateBack : PokemonCompareEvent
}
```

### Reducer (puro) — `ui/reducer/PokemonCompareReducer.kt`
```kotlin
object PokemonCompareReducer {
    fun reduce(state: PokemonCompareState, intent: PokemonCompareIntent): PokemonCompareState
    fun loading(state: PokemonCompareState, side: CompareSide): PokemonCompareState
    fun success(state: PokemonCompareState, side: CompareSide, pokemon: ComparePokemonUiModel): PokemonCompareState
    fun error(state: PokemonCompareState, side: CompareSide, error: DomainError): PokemonCompareState
}
```

### StatComparator (puro) — `comparison/StatComparator.kt`
```kotlin
// Alinha stats por label e calcula o vencedor de cada um.
fun buildStatComparison(
    first: ComparePokemonUiModel,
    second: ComparePokemonUiModel,
): List<StatComparisonRow>
```

### Mapper — `mapper/PokemonCompareUiMapper.kt`
```kotlin
fun Pokemon.toCompareUiModel(): ComparePokemonUiModel
```

### ViewModel — `viewmodel/PokemonCompareViewModel.kt`
```kotlin
class PokemonCompareViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val firstId: Int,
    private val secondId: Int,
) : ViewModel() {
    val state: StateFlow<PokemonCompareState>
    val events: Flow<PokemonCompareEvent>
    fun onIntent(intent: PokemonCompareIntent)         // Retry(side) → recarrega só aquele lado; NavigateBack → evento
    // init: carrega FIRST e SECOND em paralelo (viewModelScope.launch + async/await), cada lado atualiza seu SideUiState
}
```

### Screen / NavEntry / DI
```kotlin
@Composable
fun PokemonCompareScreen(
    firstId: Int,
    secondId: Int,
    onBack: () -> Unit,
    viewModel: PokemonCompareViewModel = koinViewModel(
        key = "pokemon_compare_${firstId}_$secondId",
        parameters = { parametersOf(firstId, secondId) },
    ),
)

fun EntryProviderScope<NavKey>.pokemonCompareEntry(navigator: AppNavigator)  // entry<PokemonCompareKey>

val pokemonCompareModule = module {
    viewModel { (firstId: Int, secondId: Int) ->
        PokemonCompareViewModel(get(), firstId, secondId)   // get() resolve GetPokemonDetailUseCase do grafo Koin
    }
}
```

## core:route:keys — `RouteKeys.kt`
```kotlin
@Serializable
data class PokemonCompareKey(val firstId: Int, val secondId: Int) : NavKey
```

## core:route:deeplink — `DeepLinkRouter.kt` (adição em `fromUrl`)
```kotlin
url.startsWith("pokedex://compare/") -> {
    val parts = url.removePrefix("pokedex://compare/").split("/")
    val a = parts.getOrNull(0)?.toIntOrNull()
    val b = parts.getOrNull(1)?.toIntOrNull()
    if (a != null && a > 0 && b != null && b > 0) PokemonCompareKey(a, b) else null   // SECURITY-05
}
```

## feature:pokemon-list (alterações)
```kotlin
// State
data class PokemonListState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val isSelectionMode: Boolean = false,
    val selectedIds: List<Int> = emptyList(),
)

// Intent (adições)
data object ToggleSelectionMode : PokemonListIntent
data class ToggleSelection(val id: Int) : PokemonListIntent
data object ClickCompare : PokemonListIntent

// Event (adições)
data class NavigateToCompare(val firstId: Int, val secondId: Int) : PokemonListEvent
data object ShowSelectionLimit : PokemonListEvent

// Reducer (transições)
// ToggleSelectionMode → copy(isSelectionMode = !mode, selectedIds = if(!mode) selectedIds else emptyList())
// ToggleSelection(id) → if (id in selectedIds) remove; else if (selectedIds.size < 2) add; else state (no-op)
// ClickCompare → state (no-op; VM trata navegação e limpeza pós-navegação, Q4=A)

// ViewModel.onIntent(adições)
//  ToggleSelection(id): se !contains(id) && size==2 → emite ShowSelectionLimit (estado inalterado)
//  ClickCompare: val ids = state.value.selectedIds; se ids.size==2 → emite NavigateToCompare(ids[0], ids[1]) e
//                limpa seleção (reduz para isSelectionMode=false, selectedIds=emptyList) [Q4=A]

// NavEntry
fun EntryProviderScope<NavKey>.pokemonListEntry(navigator: AppNavigator) {
    entry<PokemonListKey> {
        PokemonListScreen(
            onNavigateToDetail = { id -> navigator.navigateTo(PokemonDetailKey(id)) },
            onNavigateToCompare = { a, b -> navigator.navigateTo(PokemonCompareKey(a, b)) },
        )
    }
}
```
