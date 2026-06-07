# Project Structure

**Root:** `C:\projetos\android\PokedexLab`
**Modules:** 16 (declared in `settings.gradle.kts`)

## Directory Tree (3 levels)

```
PokedexLab/
├── app/                          # APK, Koin init, splash, ObjectBox+Coil init, MainActivity
│   └── src/main/java/br/com/pokedex/
│       ├── MainActivity.kt
│       ├── PokedexLabApplication.kt
│       ├── navigation/AppNavGraph.kt   # legacy/unused? see CONCERNS (AppNavDisplay is the real host)
│       └── ui/theme/                   # app-local Color/Theme/Type (duplicate of design-system)
├── build-logic/convention/       # 5 convention plugins + AndroidExtensions.kt
├── core/
│   ├── common/                   # Result<T>, DomainError, ErrorHandler, DispatcherProvider, commonModule
│   ├── design-system/            # theme/ (Color, Shape, Spacing, Theme, Type) + component/ + util/
│   ├── domain/                   # repository/PokemonRepository, usecase/Get*UseCase
│   ├── model/                    # Pokemon.kt (all domain models; NO package subdir)
│   ├── observability/            # AppLogger (Timber)
│   ├── testing/                  # FakePokemonRepository, FakePokemonData, TestDispatcherProvider, MainDispatcherRule
│   ├── ui/                       # PokemonStatBar (shared composable)
│   └── route/
│       ├── keys/                 # NavKey routes (PokemonListKey, PokemonDetailKey) + AppNavigator
│       ├── deeplink/             # DeepLinkRouter
│       └── navigation/           # AppNavDisplay (NavDisplay host) + NavBackStackNavigator
├── data/
│   ├── network/                  # PokemonApiService, dto/, paging/PokemonRemotePagingSource, source/, networkModule
│   ├── local/                    # ObjectBox entity/, mapper/, source/LocalPokemonDataSource, localModule
│   └── repository/               # PokemonRepositoryImpl, CacheStrategy, mapper/, repositoryModule
├── feature/
│   ├── pokemon-list/             # MVI + Paging3 grid
│   └── pokemon-detail/           # MVI + artwork/stats/types
├── gradle/libs.versions.toml     # version catalog (source of truth for deps)
├── memory/                       # project memory notes (tracked in git; SOME ARE STALE — see CONCERNS)
├── plans/implementation-plan.md  # phase plan (all 9 phases marked complete)
├── APOSTILA-SDD-pokemon-compare.md  # tutorial driving the in-progress "compare" feature
├── .specs/codebase/              # ← this brownfield analysis
└── CLAUDE.md                     # architectural source of truth (partially stale — see CONCERNS)
```

## Module Organization

### `:app` — composition root
**Purpose:** Koin `startKoin`, ObjectBox `BoxStore` creation, Coil image loader, splash, Activity.
**Key files:** `PokedexLabApplication.kt`, `MainActivity.kt`.

### `core:*` — shared, framework-leaning libraries
**common** (Result/DomainError/ErrorHandler/Dispatcher), **model** (domain entities),
**domain** (use cases + repo interface), **design-system** (Figma tokens + base components),
**ui** (cross-feature composables), **observability** (logging), **testing** (fakes + test rules).

### `core:route:*` — navigation (split into 3)
`keys` (pure routes + navigator interface) ← depended on by features;
`deeplink` (URL/Intent → back stack); `navigation` (NavDisplay host, aggregates feature entries).

### `data:*` — data sources & repository
`network` (Retrofit/DTOs/paging), `local` (ObjectBox), `repository` (impl + cache strategy + mappers).

### `feature:*` — user-facing screens
`pokemon-list`, `pokemon-detail`. Self-contained MVI; depend on `data:repository` + `core:*` +
`core:route:keys`, never on each other.

## Where Things Live

**Pokémon list:**
- UI: `feature/pokemon-list/.../ui/screen/PokemonListScreen.kt` (+ `component/PokemonCard.kt`)
- Logic: `.../viewmodel/PokemonListViewModel.kt`, `.../ui/reducer/PokemonListReducer.kt`
- Data: `data/network/.../paging/PokemonRemotePagingSource.kt`, `PokemonRepositoryImpl.getPokemonList`
- Route: `core/route/keys/.../RouteKeys.kt` (`PokemonListKey`), entry in feature `navigation/`

**Pokémon detail:**
- UI: `feature/pokemon-detail/.../ui/screen/PokemonDetailScreen.kt`
- Logic: `.../viewmodel/PokemonDetailViewModel.kt`, `.../ui/reducer/PokemonDetailReducer.kt`
- Data: `PokemonRepositoryImpl.getPokemonDetail` (+ species description), `LocalPokemonDataSource`
- Config: `CacheStrategy` (`data:repository`), `BASE_URL`/timeouts (`networkModule`)

## Special Directories

**`memory/`** — persistent project notes (architecture, stack, design tokens, implementation status,
AGP-9 feedback). Tracked in git. **Treat as hints, not truth** — some predate the current code
(see CONCERNS).
**`build-logic/`** — included build; convention plugins consumed via `libs.plugins.pokedexlab.*`.
**`.specs/`** — spec-driven-development workspace (this analysis lives in `.specs/codebase/`).
