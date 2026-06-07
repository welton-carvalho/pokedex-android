# Testing Infrastructure

## Test Frameworks

- **Unit:** JUnit 5 (Jupiter) 5.11.0 + `junit-jupiter-params`; `useJUnitPlatform()` enabled for all
  Android library unit tests via `LibraryExtension.configureKotlinAndroid()` (`AndroidExtensions.kt`).
- **Mocking:** MockK 1.13.12 (`coEvery`, `coVerify`, `mockk()`).
- **Coroutines:** `kotlinx-coroutines-test` 1.9.0 (`runTest`); `MainDispatcherRule` + `TestDispatcherProvider` in `core:testing`.
- **Paging:** `androidx.paging:paging-testing` (`asPagingSourceFactory`, `PagingData.from`).
- **UI / instrumented:** Compose UI test (`ui-test-junit4`, `ui-test-manifest`), AndroidX JUnit, Espresso. Wired in `:app` only; feature instrumentation runner set by `AndroidFeatureConventionPlugin`.
- **Coverage tool:** none configured.

## Test Organization

- **Location:** `<module>/src/test/kotlin/...` (JVM unit). Instrumented samples in `app/src/androidTest/`.
- **Naming:** `XxxTest.kt`; test methods use backtick sentence names (`` `Retry intent sets isLoading true` ``).
- **Shared fixtures:** `core:testing` — `FakePokemonRepository`, `FakePokemonData` (e.g. `bulbasaur`, `summaries`), `TestDispatcherProvider`, `MainDispatcherRule`.

## Existing Tests (observed)

| Test | Module | What it covers |
| --- | --- | --- |
| `ErrorHandlerTest` | `core:common` | exception → `DomainError` mapping |
| `GetPokemonListUseCaseTest`, `GetPokemonDetailUseCaseTest` | `core:domain` | use case delegates to repo |
| `PokemonRepositoryImplTest` | `data:repository` | cache-first / network fallback / single remote call — **does not compile against current `LocalPokemonDataSource` (see CONCERNS)** |
| `PokemonListReducerTest` | `feature:pokemon-list` | pure reducer transitions |
| `ExampleUnitTest`, `ExampleInstrumentedTest` | `:app` | generated stubs |

CLAUDE.md cites "~20 unit tests" covering reducers, use cases, repository.

## Test Coverage Matrix

| Code Layer | Required Test Type | Location Pattern | Run Command |
| --- | --- | --- | --- |
| Reducer (pure MVI) | unit | `feature/*/src/test/.../ui/reducer/*Test.kt` | `./gradlew :feature:<m>:testDebugUnitTest` |
| Use case | unit | `core/domain/src/test/.../usecase/*Test.kt` | `./gradlew :core:domain:test` |
| Repository / cache | unit (MockK remote + real local) | `data/repository/src/test/...` | `./gradlew :data:repository:test` |
| Error mapping | unit | `core/common/src/test/...` | `./gradlew :core:common:test` |
| Mapper (DTO/Entity/UI) | unit | none currently | — (gap → CONCERNS) |
| ViewModel | unit | none currently | — (gap → CONCERNS) |
| Paging source | unit | none currently | — (gap → CONCERNS) |
| Composable screens | instrumented (Compose UI) | `*/src/androidTest/...` | `./gradlew connectedDebugAndroidTest` |
| DeepLinkRouter | unit | none currently | — (gap → CONCERNS) |

## Parallelism Assessment

| Test Type | Parallel-Safe? | Isolation Model | Evidence |
| --- | --- | --- | --- |
| Reducer / use case unit | Yes | Pure functions, no shared mutable state | `PokemonListReducerTest`, `Get*UseCaseTest` |
| Repository unit | Conditional | Per-test MockK remote; local source state per-test instance | `PokemonRepositoryImplTest` constructs fresh sources in `@BeforeEach` (currently broken) |
| Instrumented | No (device-bound) | Single emulator/device | `app/src/androidTest` |

No shared DB connection or global mutable state across unit test files → JVM unit tests are
parallel-safe by file.

## Test Execution

```bash
./gradlew test                       # all JVM unit tests
./gradlew :feature:pokemon-list:test # one module
./gradlew assembleDebug              # full compile (verifies all modules)
./gradlew connectedDebugAndroidTest  # instrumented (needs device/emulator)
```

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | after unit-only changes in a module | `./gradlew :<module>:test` |
| Full | after cross-module / data changes | `./gradlew test` |
| Build | after phase / before merge | `./gradlew assembleDebug test` |

**Note:** Before relying on `./gradlew test` as a gate, fix the `data:repository` test compilation
(see CONCERNS) — otherwise the data layer's test task fails at compile and the gate is unreliable.
