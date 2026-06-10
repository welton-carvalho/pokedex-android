# Component Inventory

## Application Packages
- `app` - APK host, Koin init, root navigation, splash, theme.
- `feature:pokemon-list` - Paginated Pokemon list (MVI).
- `feature:pokemon-detail` - Pokemon detail screen (MVI).

## Data Packages
- `data:network` - Retrofit API, DTOs, remote paging source, remote data source.
- `data:local` - ObjectBox entities, local data source, entity mappers.
- `data:repository` - Repository impl, cache strategy, DTO/entity-to-domain mappers.

## Shared Packages (core)
- `core:common` - Result, DomainError, ErrorHandler, DispatcherProvider.
- `core:model` - Domain models (Pokemon, etc.).
- `core:domain` - Repository contract, use cases.
- `core:design-system` - Theme, tokens, base components, image URL provider.
- `core:ui` - Shared cross-feature composables (PokemonStatBar).
- `core:observability` - AppLogger (Timber), Chucker integration.
- `core:route:keys` - Navigation keys (type-safe routes) + AppNavigator.
- `core:route:navigation` - AppNavDisplay, NavBackStackNavigator.
- `core:route:deeplink` - DeepLinkRouter.

## Test Packages / Test Sources
- `core:testing` - Fakes (FakePokemonRepository, FakePokemonData), TestDispatcherProvider, MainDispatcherRule.
- Unit tests present in: `core:common`, `core:domain`, `data:repository`, `feature:pokemon-list`.

## Build Packages
- `build-logic:convention` - 5 convention plugins + AndroidExtensions helper.

## Total Count
- **Total Gradle Modules**: 17 (excluding `build-logic`).
- **Application (app + feature)**: 3
- **Data**: 3
- **Shared (core incl. route)**: 10
- **Test-support**: 1 (`core:testing`)
- **Build-logic**: 1
- **Kotlin source files (excl. build/)**: ~83
