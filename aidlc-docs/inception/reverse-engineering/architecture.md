# System Architecture

## System Overview

PokedexLab is a single-APK, multi-module Android application built with Jetpack Compose. It follows **Clean Architecture** (presentation / domain / data layering) combined with an **MVI** unidirectional-data-flow pattern inside each feature. Dependency injection is provided by Koin; the `:app` module owns `startKoin` and the root navigation. The codebase is split into 17 Gradle modules grouped under `app`, `core`, `data`, and `feature`, with build configuration centralized in convention plugins under `build-logic`.

## Architecture Diagram

```mermaid
flowchart TD
    app["app<br/>(APK, Koin init, root nav)"]

    subgraph feature_layer["feature"]
        flist["feature:pokemon-list"]
        fdetail["feature:pokemon-detail"]
    end

    subgraph data_layer["data"]
        repo["data:repository"]
        net["data:network"]
        local["data:local"]
    end

    subgraph core_layer["core"]
        domain["core:domain"]
        model["core:model"]
        common["core:common"]
        ds["core:design-system"]
        ui["core:ui"]
        obs["core:observability"]
        testing["core:testing"]
        rkeys["core:route:keys"]
        rnav["core:route:navigation"]
        rdeep["core:route:deeplink"]
    end

    extPokeAPI[(PokeAPI REST)]
    extSprites[(GitHub sprites)]
    extDB[(ObjectBox DB)]

    app --> flist
    app --> fdetail
    app --> repo
    app --> rnav
    app --> obs
    app --> ds

    rnav --> rkeys
    rnav --> rdeep
    rnav --> flist
    rnav --> fdetail
    rdeep --> rkeys

    flist --> domain
    flist --> model
    flist --> ds
    flist --> ui
    flist --> common
    flist --> rkeys
    fdetail --> domain
    fdetail --> model
    fdetail --> ds
    fdetail --> ui
    fdetail --> common
    fdetail --> rkeys

    repo --> domain
    repo --> model
    repo --> net
    repo --> local
    repo --> common
    net --> model
    net --> common
    net --> obs
    local --> model
    domain --> model
    domain --> common
    ui --> ds
    ui --> common

    net --> extPokeAPI
    flist -.coil.-> extSprites
    fdetail -.coil.-> extSprites
    local --> extDB
```

## Component Descriptions

### app
- **Purpose**: Application entry point and composition root.
- **Responsibilities**: `startKoin` with all modules, splash, root `AppNavDisplay`/nav graph, theme.
- **Dependencies**: features, data:*, core:route:navigation, core:observability, core:design-system.
- **Type**: Application.

### feature:pokemon-list / feature:pokemon-detail
- **Purpose**: Self-contained MVI screens.
- **Responsibilities**: ViewModel + State + Intent + Reducer + Event + screen + mapper + Koin DI.
- **Dependencies**: core:domain, core:model, core:design-system, core:ui, core:common, core:route:keys.
- **Type**: Application (UI feature). **Rule**: features never depend on each other.

### data:repository / data:network / data:local
- **Purpose**: Data layer.
- **Responsibilities**: repository orchestration + cache strategy; Retrofit API + DTO + remote paging source; ObjectBox entities + local source.
- **Type**: Application (data).

### core:* and core:route:*
- **Purpose**: Shared foundations — domain contracts/use cases, domain model, Result/DomainError/dispatchers, design system, shared UI, observability, testing fakes, and navigation.
- **Navigation split**: `core:route:keys` holds NavKeys + `AppNavigator`; `core:route:deeplink` maps Intent/URL → NavKey; `core:route:navigation` is the **assembly** module — its `AppNavDisplay` registers each feature's nav entry (`pokemonListEntry`, `pokemonDetailEntry`), so it (and only it) depends on the feature modules.
- **Type**: Shared library.

## Data Flow

```mermaid
sequenceDiagram
    participant U as User
    participant S as DetailScreen
    participant VM as DetailViewModel
    participant UC as GetPokemonDetailUseCase
    participant R as PokemonRepositoryImpl
    participant Net as RemoteDataSource
    participant DB as LocalDataSource

    U->>S: open detail (id)
    S->>VM: Intent.Load(id)
    VM->>UC: invoke(id)
    UC->>R: getPokemonDetail(id)
    alt NETWORK_FIRST success
        R->>Net: getPokemonDetail / species
        Net-->>R: Result.Success(dto)
        R->>DB: savePokemonDetail
        R-->>UC: Result.Success(Pokemon)
    else network error
        R->>DB: getPokemonDetail(id)
        DB-->>R: cached or null
        R-->>UC: Success(cached) or Result.Error
    end
    UC-->>VM: Result
    VM->>S: State(success/error)
    S-->>U: render
```

## Integration Points
- **External APIs**: PokeAPI REST (`https://pokeapi.co/api/v2/`) — list, detail, species.
- **Images**: PokeAPI sprites on GitHub raw (official artwork) via Coil.
- **Databases**: ObjectBox (on-device) for cached Pokemon detail/summary.
- **Third-party Services**: None server-side; Chucker for in-app network inspection (debug).

## Infrastructure Components
- **CDK Stacks**: None (client-only mobile app; no cloud IaC).
- **Deployment Model**: Android APK/AAB installed on device.
- **Networking**: Outbound HTTPS to pokeapi.co and raw.githubusercontent.com.
