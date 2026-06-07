# Concerns

Evidence-backed findings, prioritized by impact. Each has a location and a fix approach.

## High

### 1. `data:repository` unit test does not compile
**Evidence:** `data/repository/src/test/.../PokemonRepositoryImplTest.kt:53` calls
`LocalPokemonDataSource()` (no args) and `local.savePokemonDetail(...)`, but the production
`LocalPokemonDataSource` (`data/local/.../source/LocalPokemonDataSource.kt:13`) requires
`summaryBox: Box<...>` and `detailBox: Box<...>`. Git history shows ObjectBox was added
(`11cb9fb add objectbox`) **after** the tests (`16d42bb implement core:testing and unit tests`),
and the test was never updated.
**Impact:** `./gradlew :data:repository:test` (and therefore `./gradlew test`) fails at compile —
the repository's cache logic is effectively untested and the global test gate is broken.
**Fix:** extract a `LocalPokemonDataSource` interface (or an in-memory test double in `core:testing`)
so the repository test can inject a fake without a real `BoxStore`; update the test constructor.

### 2. Documentation is stale and contradicts the code
**Evidence:**
- `CLAUDE.md` describes a single `core:navigation` module with `@Serializable data object
  PokemonListRoute` / `PokemonDetailRoute`. Reality: three modules `core:route:{keys,deeplink,navigation}`
  with `PokemonListKey` / `PokemonDetailKey(pokemonId)` (`core/route/keys/.../RouteKeys.kt`).
  `PokemonListRoute`/`PokemonDetailRoute` exist **only in docs**, not in code.
- `CLAUDE.md` dependency graph omits that `core:route:navigation` depends on the feature modules
  (it aggregates `pokemonListEntry`/`pokemonDetailEntry`).
- `memory/project_implementation_status.md` marks Phases 3–9 as `⬜` (not done), but
  `plans/implementation-plan.md` and the code show all 9 phases complete.
**Impact:** anyone (human or agent) trusting CLAUDE.md/memory will reference nonexistent types and
mis-model the module graph.
**Fix:** update CLAUDE.md (navigation section + dependency graph) and the two stale `memory/*.md`
files. (Recommended to do as part of this mapping — see closing note.)

## Medium

### 3. `DispatcherProvider` is defined but never used in production
**Evidence:** `core/common/.../dispatcher/DispatcherProvider.kt` + `commonModule` declare it, but no
use case, repository, or data source injects or references it (grep: only `core:common`,
`core:testing`, and docs). Use cases call `repository` directly; ObjectBox reads in
`LocalPokemonDataSource` run synchronously on the caller's thread.
**Impact:** threading abstraction is dead code; synchronous ObjectBox/`detailBox.all` reads can run
on the Main dispatcher (use cases are invoked from `viewModelScope`, default `Main.immediate`).
**Fix:** inject `DispatcherProvider` into the repository / local source and wrap blocking I/O in
`withContext(io)`, or remove the abstraction if not wanted.

### 4. Deep links are not reachable from outside the app
**Evidence:** `DeepLinkRouter` + `AppNavDisplay(initialIntent)` handle `pokedex://list` and
`pokedex://pokemon/{id}`, but `app/src/main/AndroidManifest.xml` declares only the LAUNCHER
intent-filter — no `<data android:scheme="pokedex">` filter.
**Impact:** the deep-link code path is untestable in practice and never triggered by real Intents.
**Fix:** add an `intent-filter` with the `pokedex` scheme (and `VIEW`/`BROWSABLE`) to `MainActivity`.

### 5. Local summary cache is unused; CacheStrategy partially inert
**Evidence:** `LocalPokemonDataSource.getPokemonSummaries/savePokemonSummaries` are never called;
`PokemonRemotePagingSource` is network-only. In `PokemonRepositoryImpl.getPokemonDetail`,
`CACHE_FIRST`, `DISK`, and `MEMORY` all map to the same `fetchCacheFirst` branch — the enum implies
distinct strategies that don't exist.
**Impact:** no offline list; the `CacheStrategy` enum over-promises.
**Fix:** either implement a `RemoteMediator`/cache-backed paging and distinct strategy behaviors, or
trim the enum to what's actually supported and delete the dead summary-cache methods.

## Low

### 6. Dead / duplicated app-level files
- `app/src/main/java/br/com/pokedex/navigation/AppNavGraph.kt` is a single comment line
  (`// Navigation is handled directly in MainActivity.kt`) — dead file; remove.
- `app/src/main/java/br/com/pokedex/ui/theme/{Color,Theme,Type}.kt` duplicate the canonical theme in
  `core:design-system` (which `MainActivity` actually uses via `PokedexLabTheme`). Risk of divergence;
  delete the app-local copies if unused.

### 7. Release build does not shrink
**Evidence:** `app/build.gradle.kts` release `isMinifyEnabled = false` though `proguard-rules.pro`
exists. **Fix:** enable R8 for release once verified, or document why it's off (lab project).

### 8. HTTP body logging always on
**Evidence:** `networkModule` adds `HttpLoggingInterceptor(level = BODY)` unconditionally (not gated
by `BuildConfig.DEBUG`). **Fix:** gate at `DEBUG`, or use the no-op level in release.

### 9. `targetSdk` not configured
**Evidence:** CLAUDE.md states `targetSdk = 36`, but convention plugins set only `compileSdk = 37`
and `minSdk = 26` (`AndroidExtensions.kt`); no `targetSdk` anywhere → platform default applies.
**Fix:** set `targetSdk` in the convention plugins if 36 is intended.

### 10. Repo hygiene / build tooling
- Untracked junk at root: `bash.exe.stackdump`; untracked `.agents/`, `.cursor/`. Add to `.gitignore`.
- `ksp` is declared in the catalog but unused; `data:local` uses `kapt` (`com.android.legacy-kapt`),
  which is slower. Consider whether ObjectBox can move off kapt, or drop the unused `ksp` entry.

---

**Note:** This is an architecture **lab** project (per `memory/project_architecture.md`) — decisions
favor learnability over production hardening, so items 5/7/8/9 may be intentional. Items 1–4 and 6
are genuine inconsistencies worth fixing regardless.
