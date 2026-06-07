# Tech Stack

**Analyzed:** 2026-06-07
**Source of truth:** `gradle/libs.versions.toml` (version catalog), convention plugins in `build-logic/`

## Core

- Build system: Gradle (Kotlin DSL), wrapper 9.3.1
- Android Gradle Plugin: 9.1.1
- Language: Kotlin 2.2.10 (JVM target 11, `sourceCompatibility`/`targetCompatibility` = 11)
- KSP: 2.2.10-1.0.31 (declared in catalog; **not currently applied** by any module — see CONCERNS)
- Kotlin Serialization plugin: 2.2.21; kotlinx-serialization-json 1.9.0
- Package manager: Gradle version catalog (`libs.versions.toml`)
- Annotation processing: `kapt` via `com.android.legacy-kapt` (used only by `data:local` for ObjectBox)

**SDK config** (centralized in `build-logic/.../AndroidExtensions.kt`):
- `minSdk = 26`
- `compileSdk = 37` (required by `material3-adaptive-navigation3:1.3.0-beta01`)
- `targetSdk` — set per CLAUDE.md as 36, but **not configured in convention plugins** (no `targetSdk` line in `AndroidExtensions.kt`)

## Frontend (UI)

- UI Framework: Jetpack Compose, BOM `2026.02.01` + Material3
- Material icons extended (`material-icons-extended`)
- Styling: custom design system (`core:design-system`) — Figma tokens, Poppins typography, type colors
- State Management: MVI (hand-rolled: `State` + `Intent` + pure `Reducer` + `Event` channel + `ViewModel` exposing `StateFlow`)
- Navigation: **Navigation3** (`androidx.navigation3` 1.1.1) — `NavDisplay` + `rememberNavBackStack` + `entryProvider`; `lifecycle-viewmodel-navigation3` 2.11.0-beta01; `material3-adaptive-navigation3` 1.3.0-beta01
- Splash: `androidx.core:core-splashscreen` 1.0.1
- Image loading: Coil 3.4.0 (`coil3`) with OkHttp network layer + memory/disk cache

## Backend / Data

- API Style: REST via Retrofit 3.0.0
- JSON: kotlinx.serialization (`converter-kotlinx-serialization`), `Json { ignoreUnknownKeys = true }`
- HTTP client: OkHttp with `HttpLoggingInterceptor` (BODY) + Chucker interceptor; 30s connect/read/write timeouts
- Local DB: ObjectBox 5.4.2 (`io.objectbox` Gradle plugin + kapt processor); `objectbox-android-objectbrowser` in debug, `objectbox-android` in release
- Paging: Paging 3 (3.3.6) — `Pager` + `PagingSource`, `paging-compose`, `paging-testing`
- Authentication: none (public PokeAPI)

## DI

- Koin 4.2.1 (`koin-android`, `koin-compose`, `koin-compose-viewmodel`), `koin-test`

## Testing

- Unit: JUnit 5 (Jupiter) 5.11.0 + params; `useJUnitPlatform()` enabled for library unit tests
- Mocking: MockK 1.13.12 (`mockk-android`, `mockk-agent`)
- Coroutines: `kotlinx-coroutines-test` 1.9.0
- Paging tests: `androidx.paging:paging-testing`
- UI/instrumented: Compose UI test (`ui-test-junit4`, `ui-test-manifest`), AndroidX JUnit 1.3.0, Espresso 3.7.0
- Note: legacy JUnit4 (4.13.2) still present for `:app` `testImplementation`

## Observability / Tooling

- Logging: Timber 5.0.1, wrapped by `core:observability` `AppLogger` (Debug tree only when `BuildConfig.DEBUG`)
- Network inspector: Chucker 4.0.0 (`library` + `library-no-op`)
- Foojay toolchains resolver 1.0.0 (JDK auto-provisioning)

## Repositories

- `google()`, `mavenCentral()`, `jitpack.io` (Chucker). `FAIL_ON_PROJECT_REPOS` enforced.
