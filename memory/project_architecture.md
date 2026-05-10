---
name: PokedexLab architecture overview
description: Module structure, dependency graph, and key architectural decisions
type: project
---

Multi-module Android lab project using Jetpack Compose + MVI + Clean Architecture.

**Why:** Serves as an architecture laboratory — each phase introduces a new pattern or library for study. Decisions should favor learnability and experimentation over simplicity.

**16 modules:**
- `:app` — entry point, Koin init, splash, NavGraph
- `core:common` — DomainError, Result<T>, ErrorHandler, DispatcherProvider
- `core:design-system` — Figma tokens: PokedexRed #DC0A2D, 18 type colors, Poppins typography
- `core:domain` — PokemonRepository interface, GetPokemonListUseCase, GetPokemonDetailUseCase
- `core:model` — Pokemon, PokemonSummary, PokemonType, PokemonStat, PokemonAbility
- `core:navigation` — PokemonListRoute, PokemonDetailRoute (serializable)
- `core:observability` — AppLogger (Timber), Chucker
- `core:testing` — fakes, TestDispatcherProvider
- `core:ui` — PokemonStatBar and shared composables
- `data:network` — Retrofit 3.0, DTOs, RemoteDataSource
- `data:local` — ObjectBox 5.4.2, entities, LocalDataSource
- `data:repository` — PokemonRepositoryImpl, CacheStrategy enum
- `feature:pokemon-list` — Paging3 list with MVI
- `feature:pokemon-detail` — detail screen with MVI

**How to apply:** Check module ownership before adding any new file. Features never import from each other.
