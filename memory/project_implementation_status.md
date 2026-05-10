---
name: PokedexLab implementation status
description: Which phases are done and what is pending
type: project
---

As of 2026-05-10:

**✅ Fase 1 — Fundação**
- build-logic with 5 convention plugins
- libs.versions.toml with all libraries
- settings.gradle.kts with all 16 modules declared

**✅ Fase 2 — Core modules**
- core:model, core:common, core:domain, core:navigation, core:observability, core:design-system, core:ui
- CLAUDE.md created at project root

**⬜ Fase 3 — Data layer**
- data:network (Retrofit, DTOs, RemotePokemonDataSource, NetworkModule)
- data:local (ObjectBox entities, LocalPokemonDataSource, LocalModule)
- data:repository (PokemonRepositoryImpl, CacheStrategy, RepositoryModule)

**⬜ Fase 4 — App shell**
- PokedexLabApplication (Koin startKoin)
- Splash Screen API
- MainActivity with Navigation3 host + NavGraph

**⬜ Fase 5 — feature:pokemon-list**
- Full MVI: State/Intent/Reducer/Event + ViewModel
- Paging3 LazyColumn (50 items/page, cachedIn)
- PokemonCard component matching Figma (104x108dp, 3-column grid)

**⬜ Fase 6 — feature:pokemon-detail**
- Full MVI
- Official artwork, type chips, stat bars, abilities, weight/height

**⬜ Fase 7-8 — Testing**
**⬜ Fase 9 — Polish**

**Why:** Tracks where to resume in the next session without re-reading all files.

**How to apply:** Update this file at the end of each session.
