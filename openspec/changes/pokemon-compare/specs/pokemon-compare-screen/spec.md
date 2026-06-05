# Spec Delta: pokemon-compare-screen

## ADDED Requirements

### Requirement: Compare screen loads both Pokémon in parallel
WHEN the compare screen is opened with `PokemonCompareKey(idA, idB)`,
the system SHALL load both Pokémon concurrently via `GetPokemonDetailUseCase` and display each side with an independent loading state.

#### Scenario: Both sides load successfully
- GIVEN the compare screen receives `idA = 1` and `idB = 4`
- WHEN both `GetPokemonDetailUseCase` calls complete successfully
- THEN side A displays Bulbasaur's artwork, name (#001), type chips, height, weight, and stat bars
- AND side B displays Charmander's artwork, name (#004), type chips, height, weight, and stat bars

#### Scenario: Side A is loading while side B has already loaded
- GIVEN `GetPokemonDetailUseCase(idA)` is still in-flight
- WHEN `GetPokemonDetailUseCase(idB)` completes first
- THEN side B shows success content
- AND side A shows a loading indicator independently

### Requirement: Compare screen displays mirrored stat bars
WHEN both Pokémon are loaded successfully,
the system SHALL display one `CompareStatRow` per stat (HP, Attack, Defense, Sp. Atk, Sp. Def, Speed) with the stat value and bar for each side mirrored symmetrically, using the respective Pokémon's primary type color.

#### Scenario: Stat bar colors reflect Pokémon type
- GIVEN side A is a Grass-type Pokémon and side B is a Fire-type Pokémon
- WHEN the compare screen renders the stat section
- THEN side A's stat bars use the Grass type color
- AND side B's stat bars use the Fire type color

#### Scenario: CompareStatRow reuses PokemonStatBar without code duplication
- GIVEN both sides are loaded
- WHEN the UI renders a stat row
- THEN `CompareStatRow` delegates bar rendering to the existing `PokemonStatBar` composable
- AND no bar-drawing logic is copied or reimplemented inside `CompareStatRow`

### Requirement: Compare screen handles error per side with retry
WHEN `GetPokemonDetailUseCase` fails for one or both sides,
the system SHALL display an error state for the affected side only, with a retry action that re-triggers the load for that side via `RetryPokemon(side: Side)` intent.

`CompareIntent` subtypes and `Side` type (declared in `feature:pokemon-compare`):
```kotlin
enum class Side { A, B }

sealed interface CompareIntent {
    data class Load(val idA: Int, val idB: Int) : CompareIntent
    data class RetryPokemon(val side: Side) : CompareIntent
}
```

#### Scenario: One side errors, other succeeds
- GIVEN `GetPokemonDetailUseCase(idA)` returns a `DomainError.Network`
- WHEN the compare screen renders
- THEN side A shows an error UI with a retry button
- AND side B shows its success content unaffected

#### Scenario: Retry recovers the failed side
- GIVEN side A is in error state
- WHEN the user taps the retry button on side A
- THEN `RetryPokemon(Side.A)` intent is dispatched
- AND side A transitions to loading state, then success or error based on the new call result

### Requirement: Compare screen displays artwork, types, height and weight for each side
WHEN both sides have loaded successfully,
the system SHALL display for each side: the official artwork image, the Pokémon name and formatted number, the type chip(s), and height and weight values in a two-column row layout.

#### Scenario: Artwork and metadata visible after load
- GIVEN both `GetPokemonDetailUseCase` calls succeed
- WHEN the compare screen renders
- THEN side A shows `PokemonImageUrlProvider.officialArtwork(idA)` image, name, number, type chips, height, and weight
- AND side B shows the same data for Pokémon B in the mirrored column

### Requirement: Compare screen handles invalid input defensively
WHEN `idA == idB` is detected on screen entry,
the system SHALL set `CompareState.isInvalidInput = true` — a field in the existing `data class CompareState` — and not trigger any network calls. The screen renders a single screen-level error message instead of the per-side layout.

`CompareState` shape:
```kotlin
data class CompareState(
    val isInvalidInput: Boolean = false,
    val pokemonA: AsyncResult<CompareUiModel> = AsyncResult.Loading,
    val pokemonB: AsyncResult<CompareUiModel> = AsyncResult.Loading,
)
```
`CompareUiModel` shape (defined in `feature:pokemon-compare`, mapped from `Pokemon` domain model):
```kotlin
data class CompareUiModel(
    val id: Int,
    val name: String,              // e.g. "Bulbasaur"
    val formattedNumber: String,   // e.g. "#001"
    val imageUrl: String,          // officialArtwork URL
    val types: List<String>,       // e.g. ["Grass", "Poison"]
    val primaryTypeColor: Color,   // pokemonTypeColor(types.first())
    val heightMeters: Float,       // e.g. 0.7
    val weightKg: Float,           // e.g. 6.9
    val stats: List<StatUiModel>,  // ordered: HP, Attack, Defense, Sp. Atk, Sp. Def, Speed
)

data class StatUiModel(
    val label: String,  // e.g. "HP", "ATK", "DEF", "SPA", "SPD", "SPE"
    val value: Int,     // raw stat value
    val maxValue: Int = 255,
)
```

`AsyncResult<T>` is a sealed interface declared in `core:common` (not inside `feature:pokemon-compare`):
```kotlin
// core:common — shared across features
sealed interface AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val error: DomainError) : AsyncResult<Nothing>
}
```
When `isInvalidInput = true`, `pokemonA` and `pokemonB` are not used by the UI.

#### Scenario: Same Pokémon IDs passed as both sides
- GIVEN the screen receives `PokemonCompareKey(idA = 1, idB = 1)`
- WHEN the ViewModel initializes
- THEN no `GetPokemonDetailUseCase` is called
- AND `state.isInvalidInput = true`
- AND the screen renders a full-screen invalid-input message (not the per-side layout)

### Requirement: Compare screen content is scrollable
WHEN the compare screen renders all sections (artwork, types, height/weight, stats),
the system SHALL present the content using `Column + Modifier.verticalScroll` (not `LazyColumn`, since content is fixed-size and not paginated) so that the full content is accessible by vertical scrolling.

#### Scenario: User scrolls to see all stats
- GIVEN both sides are loaded and the screen renders all 6 stat rows
- WHEN the user scrolls down
- THEN the stat rows that were off-screen become visible without clipping

## MODIFIED Requirements

## REMOVED Requirements
