# Architecture

**Pattern:** Modular Clean Architecture + MVI, 16 Gradle modules. Convention-plugin–driven build. Single-Activity Compose app with Navigation3.

## High-Level Structure

```
                         ┌─────────┐
                         │  :app   │  Koin startKoin, splash, ObjectBox init, Coil init
                         └────┬────┘
                              │ depends on
        ┌─────────────────────┼───────────────────────────────┐
        ▼                     ▼                                ▼
core:route:navigation   feature:pokemon-list           data:repository
 (AppNavDisplay)        feature:pokemon-detail          data:network / data:local
        │                     │                                │
        │ aggregates          ▼                                ▼
        └──────────────► core:domain ◄───── core:common   core:model
                         (UseCase + Repository interface)
```

### Layering (dependency direction points inward)

```
feature:*  ──►  core:domain (UseCase, Repository interface)
data:repository (impl)  ──►  core:domain  +  data:network + data:local
core:domain  ──►  core:model + core:common
```

The domain layer (`core:domain`) holds the `PokemonRepository` interface and use cases; the
implementation (`PokemonRepositoryImpl`) lives in `data:repository`. Features depend on the
domain interface and call use cases — they never touch data sources directly. Koin wires the
implementation to the interface at app startup.

## Identified Patterns

### MVI per feature
**Location:** `feature/*/src/main/kotlin/.../ui/{state,intent,reducer,event}` + `viewmodel/`
**Purpose:** Unidirectional data flow with a pure, testable reducer.
**Implementation:** `ViewModel` holds `MutableStateFlow<XxxState>`; `onIntent()` runs
`XxxReducer.reduce(state, intent)` (pure `(State, Intent) -> State`), then dispatches side effects
(navigation, async loads) and emits ephemeral `XxxEvent`s via a `Channel(...).receiveAsFlow()`.
**Example:** `feature/pokemon-detail/.../viewmodel/PokemonDetailViewModel.kt`,
`feature/pokemon-list/.../ui/reducer/` (reducer is the only unit-tested UI layer).

Note: the **list** screen drives loading/empty/error state through Paging 3's `LoadState`, so its
reducer only handles `Retry`/`ClickPokemon`; the **detail** screen drives state imperatively from a
suspend use case (`loading`/`success`/`error` reducer helpers).

### Repository + CacheStrategy
**Location:** `data:repository` — `PokemonRepositoryImpl`, `CacheStrategy` enum.
**Purpose:** Configurable read policy across remote (Retrofit) and local (ObjectBox) sources.
**Implementation:** `getPokemonDetail(id)` branches on `CacheStrategy` (default `NETWORK_FIRST`):
network-first falls back to cache on error; cache-first reads ObjectBox (1-hour TTL) before network.
Detail fetch also enriches with species "flavor text" (English description). `getPokemonList()`
returns a `Pager(...).flow` backed by `PokemonRemotePagingSource` (network-only paging; the local
summary cache exists but is **not consulted** in paging — see CONCERNS).

### Navigation3 with NavKey + aggregator module
**Location:** `core:route:*` (three modules).
- `core:route:keys` — `@Serializable` `NavKey`s (`PokemonListKey`, `PokemonDetailKey(pokemonId)`) and the `AppNavigator` interface. Pure, depended on by features.
- `core:route:deeplink` — `DeepLinkRouter` maps `pokedex://list` and `pokedex://pokemon/{id}` URLs/Intents to a synthetic back stack.
- `core:route:navigation` — `AppNavDisplay` (the `NavDisplay` host) and `NavBackStackNavigator` (`AppNavigator` impl over `NavBackStack`). **This module aggregates the feature nav entries** — it imports `pokemonListEntry`/`pokemonDetailEntry`.

Each feature contributes an `EntryProviderScope<NavKey>.xxxEntry(navigator)` extension under its
`navigation/` package, keeping the route→screen wiring inside the feature while `core:route:navigation`
composes them. **Features still never depend on each other** — only on `core:route:keys`.

### Convention plugins
**Location:** `build-logic/convention/src/main/kotlin/`.
**Purpose:** Centralize Android/Kotlin/Compose config; avoid per-module boilerplate.
**Implementation:** `AndroidLibraryConventionPlugin` applies `com.android.library` only (AGP 9.x
registers the `kotlin` extension internally — applying `kotlin.android` explicitly throws). Compose
modules add `org.jetbrains.kotlin.plugin.compose`; feature modules add serialization + the
instrumentation runner via `AndroidFeatureConventionPlugin`. Shared SDK/compiler config in
`AndroidExtensions.kt`.

## Data Flow

### List (paged)
`PokemonListScreen` (`collectAsLazyPagingItems`) → `PokemonListViewModel.pokemonPagingFlow`
→ `GetPokemonListUseCase()` → `PokemonRepository.getPokemonList()` → `Pager` +
`PokemonRemotePagingSource` → `RemotePokemonDataSource` → Retrofit `GET /pokemon/?offset&limit=50`.
DTOs mapped to `PokemonSummary` in the paging source; ViewModel maps domain→`PokemonListUiModel`
with `pagingData.map { it.toUiModel() }`, `cachedIn(viewModelScope)`.

### Detail
`PokemonDetailScreen` → `PokemonDetailViewModel.loadPokemon()` → `GetPokemonDetailUseCase(id)`
→ `PokemonRepositoryImpl.getPokemonDetail` → (`NETWORK_FIRST`) `RemotePokemonDataSource.getPokemonDetail`
+ `getPokemonSpecies` (description) → save to `LocalPokemonDataSource` (ObjectBox) → `Result<Pokemon>`
→ reducer `success/error` → `PokemonDetailUiModel`. Errors are `DomainError` (mapped by
`ErrorHandler` in `core:common`), never raw exceptions in the UI.

### Navigation / deep links
`MainActivity` → `AppNavDisplay(initialIntent)`. `DeepLinkRouter.fromIntent` builds the initial
back stack (defaults to `[PokemonListKey]`). `NavBackStackNavigator` mutates the `NavBackStack`.
Detail→detail "next pokemon" navigation: pop then push `PokemonDetailKey(nextId)`.

## Code Organization

**Approach:** Layer-based at the top (core / data / feature), feature-based within `feature:*`.
**Module boundaries:** enforced by Gradle `implementation(project(...))` edges + the "features
never know each other" rule. Domain interface / data impl split decouples features from data libs.
**App composition root:** `PokedexLabApplication.onCreate` builds `MyObjectBox`, registers the
`BoxStore` into Koin, then loads `commonModule, networkModule, localModule, repositoryModule,
pokemonListModule, pokemonDetailModule`.
