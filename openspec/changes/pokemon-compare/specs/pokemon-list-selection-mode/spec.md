# Spec Delta: pokemon-list-selection-mode

## ADDED Requirements

### Requirement: User can toggle compare-selection mode on the list screen
WHEN the user activates compare-selection mode,
the system SHALL set `isCompareMode = true` in `PokemonListState` and render all cards with the selection overlay UI (unselected cards at alpha 40%).

#### Scenario: Entering compare mode
- GIVEN the list screen is in normal mode (`isCompareMode = false`, `selectedForCompare = emptySet()`)
- WHEN the user taps the compare toggle button
- THEN `ToggleCompareMode` intent is dispatched
- AND `isCompareMode` becomes `true`
- AND all cards render with the alpha dimming effect (unselected = 40%)

#### Scenario: Exiting compare mode clears selection
- GIVEN the list screen is in compare mode with 1 Pokémon selected
- WHEN the user taps the compare toggle button again
- THEN `ToggleCompareMode` intent is dispatched
- AND `isCompareMode` becomes `false`
- AND `selectedForCompare` is cleared to `emptySet()`

### Requirement: User selects up to two distinct Pokémon for comparison
WHEN the user is in compare-selection mode and taps a Pokémon card,
the system SHALL apply the following rules via `ToggleSelectForCompare(id: Int)` intent:
(a) `|selected| < 2` and id not in set → add to set;
(b) id already in set → remove from set (deselect);
(c) `|selected| == 2` and id not in set → ignore tap and emit `ShowSelectionLimitReached : PokemonListEvent` (snackbar/toast shown by the host — never stored in `PokemonListState`);
(d) the same id cannot appear twice (guaranteed by `Set<Int>`).

#### Scenario: Selecting first Pokémon
- GIVEN compare mode is active and `selectedForCompare = emptySet()`
- WHEN the user taps Bulbasaur (id = 1)
- THEN `selectedForCompare = {1}`
- AND Bulbasaur's card renders as selected (full alpha)
- AND all other cards remain at alpha 40%

#### Scenario: Selecting second Pokémon triggers navigation
- GIVEN `selectedForCompare = {1}`
- WHEN the user taps Charmander (id = 4)
- THEN `selectedForCompare = {1, 4}`
- AND `NavigateToCompare(idA = 1, idB = 4)` event is emitted
- AND the navigator navigates to `PokemonCompareKey(1, 4)`

#### Scenario: Tapping selected Pokémon deselects it
- GIVEN `selectedForCompare = {1}`
- WHEN the user taps Bulbasaur (id = 1) again
- THEN `selectedForCompare = emptySet()`
- AND all cards return to alpha 40%

#### Scenario: Tapping third Pokémon when two are already selected is ignored
- GIVEN `selectedForCompare = {1, 4}`
- WHEN the user taps Squirtle (id = 7)
- THEN `selectedForCompare` remains `{1, 4}` unchanged
- AND `ShowSelectionLimitReached` event is emitted (rendered as snackbar by the host — not stored in state)

#### Scenario: Same Pokémon cannot be selected twice (set guarantee)
- GIVEN `selectedForCompare = {1}`
- WHEN a second `ToggleSelectForCompare(id = 1)` arrives (e.g., rapid double-tap)
- THEN `selectedForCompare = emptySet()` (deselect, not duplicate)
- AND no navigation is triggered

### Requirement: Compare mode resets after returning from compare screen
WHEN the user navigates back from the compare screen to the list screen,
the system SHALL reset compare-mode state by dispatching `ResetCompareMode : PokemonListIntent` from the list screen composable via a `LifecycleResumeEffect` (Navigation3 lifecycle hook), which causes the reducer to set `isCompareMode = false` and `selectedForCompare = emptySet()`.

MVI mechanism:
- `PokemonListScreen` registers a `LifecycleResumeEffect` that dispatches `ResetCompareMode` on each resume
- `PokemonListReducer` handles `ResetCompareMode` → `state.copy(isCompareMode = false, selectedForCompare = emptySet())`
- No Boolean flag in `PokemonListState` is added for this — reset is triggered by lifecycle

#### Scenario: List resets after back navigation
- GIVEN the user is in compare mode with `selectedForCompare = {1, 4}` and navigated to the compare screen
- WHEN the user presses back and returns to the list screen
- THEN `LifecycleResumeEffect` fires and dispatches `ResetCompareMode`
- AND `isCompareMode = false` and `selectedForCompare = emptySet()`
- AND all cards render at full alpha with no selection overlay

### Requirement: List screen navigation to compare preserves feature isolation
WHEN `NavigateToCompare(idA, idB)` event is emitted from `feature:pokemon-list`,
the system SHALL communicate exclusively via `@Serializable data class PokemonCompareKey(val idA: Int, val idB: Int) : NavKey` from `core:navigation` — `feature:pokemon-list` MUST NOT import any class from `feature:pokemon-compare`.

`PokemonListEvent` subtypes introduced by this change:
```kotlin
data class NavigateToCompare(val idA: Int, val idB: Int) : PokemonListEvent
data object ShowSelectionLimitReached : PokemonListEvent
```

MVI event-emission contract: the **ViewModel** is responsible for emitting `PokemonListEvent` (not the reducer). The reducer remains a pure `(PokemonListState, PokemonListIntent) → PokemonListState` function. The ViewModel observes the state after each intent and emits events as side effects.

#### Scenario: Navigation event dispatched correctly
- GIVEN two Pokémon are selected (ids 1 and 4)
- WHEN `ToggleSelectForCompare(id = 4)` is processed and `|selectedForCompare| == 2`
- THEN the ViewModel emits `NavigateToCompare(idA = 1, idB = 4)` as a `PokemonListEvent`
- AND the event handler in the screen calls `navigator.navigateTo(PokemonCompareKey(1, 4))`

## MODIFIED Requirements

## REMOVED Requirements
