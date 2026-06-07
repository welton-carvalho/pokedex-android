# Code Conventions

Derived from sampling ~20 representative files across all layers. Document reflects observed,
consistent practice.

## Naming Conventions

**Language:** All identifiers, packages, files, and class names in **English**. (User-facing prose
in some KDoc/comments is Portuguese — e.g. `DeepLinkRouter`.)

**Packages:** `br.com.pokedex.<area>.<layer>` — e.g. `br.com.pokedex.feature.pokemonlist.ui.reducer`,
`br.com.pokedex.data.network.dto`, `br.com.pokedex.core.route.keys`.
Note: module Gradle names are kebab-case (`pokemon-list`, `design-system`) but the Kotlin package
collapses them (`pokemonlist`, `designsystem`).

**Files:** PascalCase matching the primary type (`PokemonRepositoryImpl.kt`, `AppNavDisplay.kt`).
DI module files are named `XxxModule.kt` and expose a top-level `val xxxModule = module { ... }`.

**Classes/Interfaces:** PascalCase. Suffix conventions:
- `*Impl` for the single implementation of a domain interface (`PokemonRepositoryImpl`)
- `*UseCase` for use cases (`GetPokemonListUseCase`)
- `*ViewModel`, `*Reducer`, `*State`, `*Intent`, `*Event`, `*UiModel`, `*UiMapper`
- `*Dto` for network models, `*Entity` for ObjectBox models, `*Key` for `NavKey` routes
- `*DataSource` (`RemotePokemonDataSource`, `LocalPokemonDataSource`)

**Functions:** camelCase. Mappers are extension functions named `toDomain()`, `toEntity()`,
`toUiModel()`, `toDetailUiModel()`. UI entry points are `@Composable` PascalCase (`PokemonCard`).
Nav entries are `EntryProviderScope<NavKey>.xxxEntry(navigator)`.

**Constants:** `UPPER_SNAKE_CASE`, declared `private const val` at file top
(`PAGE_SIZE`, `PREFETCH_DISTANCE`, `BASE_URL`, `TIMEOUT_SECONDS`, `CACHE_TTL_MS`).

## Code Organization

**Imports:** Explicit (no wildcards). Standard IDE ordering. No import aliases observed.

**MVI feature layout (mandatory):**
```
ui/{component,model,screen,state,intent,reducer,event}/ + viewmodel/ + mapper/ + di/ + navigation/
```
Reducers are `object`s with a pure `reduce(state, intent): State` plus optional named helpers
(`loading`, `success`, `error`). State classes are `data class` with defaults, marked `@Immutable`.

**File structure within a file:** `private const val` constants → primary type → top-level extension
helpers (e.g. `Result.onSuccess/onError/map` co-located in `Result.kt`).

## Type Safety / Models

- Domain models (`core:model`) are plain `data class` (`Pokemon`, `PokemonSummary`, `PokemonType`,
  `PokemonStat`, `PokemonAbility`), framework-free.
- Layer separation is strict: `*Dto` (network) → domain → `*UiModel` (per feature), via extension
  mappers. Entities (`*Entity`) bridge domain ↔ ObjectBox.
- Routes are type-safe `@Serializable` `NavKey`s.
- Result type: `sealed interface Result<out T>` with `Success`/`Error(DomainError)` (no exceptions
  cross layer boundaries).

## Error Handling

**Pattern:** Exceptions are caught at the edge (data source / paging source) and converted to
`DomainError` via `core:common` `ErrorHandler.handle(throwable)` (maps `SocketTimeout→Timeout`,
`UnknownHost`/`IOException→Network`, else `Unknown`). Domain/feature layers consume `Result<T>` and
`DomainError` only. Paging surfaces failure through `LoadState.Error`.

## DI Conventions

- One Koin `module` per Gradle module, exposed as a top-level `val`.
- `single` by default; `factory` for use cases; `viewModel { }` (koin-compose-viewmodel) for VMs.
- `:app` is the only `startKoin` site and lists every module explicitly.

## Comments / Documentation

Sparse. Comments appear only to explain non-obvious build/runtime constraints (e.g. the ObjectBox
`debugRuntimeClasspath` exclusion in `app/build.gradle.kts`, the AGP-9 Kotlin-plugin note in
`AndroidLibraryConventionPlugin`, BoxStore registration order in `localModule`). Prefer this style:
comment the "why" of a constraint, not the "what".
