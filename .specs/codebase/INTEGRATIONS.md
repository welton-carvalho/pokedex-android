# External Integrations

## REST API — PokeAPI

**Service:** PokeAPI v2 (public, unauthenticated)
**Purpose:** Source of all Pokémon list/detail/species data.
**Location:** `data/network/.../api/PokemonApiService.kt` (Retrofit interface),
`source/RemotePokemonDataSource.kt`, `paging/PokemonRemotePagingSource.kt`.
**Configuration:** `networkModule` — `BASE_URL = "https://pokeapi.co/api/v2/"`, 30s timeouts,
Retrofit 3.0 + kotlinx.serialization converter (`Json { ignoreUnknownKeys = true }`).
**Authentication:** none.

**Endpoints used:**
- `GET pokemon/?offset={offset}&limit={limit}` → `PokemonListResponseDto` (paged, 50/page)
- `GET pokemon/{id}` → `PokemonDetailDto` (types, stats, abilities, height, weight)
- `GET pokemon-species/{id}` → `PokemonSpeciesDto` (English flavor text → `Pokemon.description`)

**Resilience:** errors mapped to `DomainError` via `ErrorHandler`; detail reads fall back to the
ObjectBox cache on network failure (`NETWORK_FIRST`).

## Image CDN — PokeAPI sprites (via Coil)

**Service:** `raw.githubusercontent.com/PokeAPI/sprites/.../official-artwork/{id}.png`
**Purpose:** Official artwork for cards and detail screen.
**Location:** `core/design-system/.../util/PokemonImageUrlProvider.kt` (URL builder); loaded by Coil 3.
**Configuration:** Coil `ImageLoader` in `PokedexLabApplication.initCoil()` — memory cache (20% of
app memory), disk cache (50 MB at `cacheDir/image_cache`), crossfade enabled, OkHttp network layer.

## Local Database — ObjectBox

**Service:** ObjectBox 5.4.2 embedded NoSQL store.
**Purpose:** Cache for Pokémon detail (1-hour TTL) and summaries.
**Location:** `data/local/` — `entity/PokemonDetailEntity`, `entity/PokemonSummaryEntity`,
`source/LocalPokemonDataSource`, `mapper/PokemonEntityMapper`, `localModule`.
**Configuration:** `MyObjectBox` (kapt-generated) built in `PokedexLabApplication.onCreate`; the
`BoxStore` is injected into Koin so `localModule` can resolve `Box<...>` instances. ObjectBrowser
**Admin** UI started in debug builds (`Admin(boxStore).start(this)`).
**Build note:** `objectbox-android-objectbrowser` bundles `objectbox-android`; `:app` excludes the
latter from `debugRuntimeClasspath` to avoid duplicate classes.

## Network Inspector — Chucker

**Service:** Chucker 4.0.0.
**Purpose:** On-device HTTP inspection (debug).
**Location:** `networkModule` — `ChuckerInterceptor` added to the OkHttp client (alongside
`HttpLoggingInterceptor` at BODY level). `library` / `library-no-op` variants available in the catalog.

## Logging — Timber

**Service:** Timber 5.0.1, wrapped by `core:observability` `AppLogger`.
**Location:** `AppLogger.init(BuildConfig.DEBUG)` in `PokedexLabApplication` plants a `DebugTree`
only in debug. Exposes `d/e/w/i` helpers.

## Deep Links

**Source:** `pokedex://` custom scheme.
**Location:** `core/route/deeplink/.../DeepLinkRouter.kt`; consumed by `AppNavDisplay(initialIntent)`.
**Events handled:** `pokedex://list` → list; `pokedex://pokemon/{id}` → detail (synthetic back stack
`[list, detail]`).
**⚠️ Not wired end-to-end:** `app/src/main/AndroidManifest.xml` declares no `intent-filter` for the
`pokedex` scheme, so external deep links cannot currently reach the app (see CONCERNS).

## Background Jobs

None. No WorkManager / queue / scheduler. Paging prefetch is the only background data activity.
