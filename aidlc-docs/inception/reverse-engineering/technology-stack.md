# Technology Stack

## Programming Languages
- Kotlin - 2.2.10 - all application and build-logic code.

## Frameworks & Libraries
- Jetpack Compose + Material3 - BOM 2026.02.01 - UI.
- Koin - 4.2.1 - dependency injection.
- Retrofit - 3.0.0 - REST networking.
- Coil - 3.4.0 - image loading.
- ObjectBox - 5.4.2 - local NoSQL persistence (KSP-generated).
- Paging 3 - 3.3.6 - list pagination.
- Navigation3 - 1.1.1 - type-safe navigation (NavKey/NavDisplay).
- Timber - 5.0.1 - logging.
- Chucker - 4.0.0 - in-app network inspector (debug).
- kotlinx.coroutines / Flow - async + reactive streams.
- kotlinx.serialization - serializable nav keys / JSON.

## Platform / SDK
- minSdk 26, targetSdk 36, compileSdk 37 (required by material3-adaptive-navigation3:1.3.0-beta01).

## Build Tools
- Gradle (Kotlin DSL) - 9.3.1.
- Android Gradle Plugin - 9.1.1.
- KSP - ObjectBox annotation processing.
- Convention plugins (build-logic) - shared module config.
- Version catalog - `gradle/libs.versions.toml`.

## Testing Tools
- JUnit 5 - 5.11.0 - unit test runner.
- MockK - 1.13.12 - mocking.
- Compose UI Tests - instrumented UI testing.
- core:testing fakes - in-memory test doubles.

## External Services
- PokeAPI (`pokeapi.co`) - Pokemon data.
- GitHub raw (PokeAPI sprites) - official artwork images.
