---
name: PokedexLab implementation status
description: Which phases are done and what is pending
type: project
---

As of 2026-06-07 (verified against code during brownfield mapping):

**Phases 1–9 all ✅ complete** (confirmed by `plans/implementation-plan.md` and the source tree):
- Fase 1 Fundação — build-logic (5 convention plugins), libs.versions.toml, 16 modules
- Fase 2 Core modules — model, common, domain, route (keys/deeplink/navigation), observability, design-system, ui
- Fase 3 Data layer — network (Retrofit+Paging3), local (ObjectBox+kapt), repository (CacheStrategy)
- Fase 4 App shell — PokedexLabApplication (Koin+ObjectBox+Coil), splash, MainActivity → AppNavDisplay
- Fase 5 pokemon-list — MVI + Paging3 grid
- Fase 6 pokemon-detail — MVI + artwork/stats/types/description
- Fase 7–8 Testing — core:testing fakes + ~20 unit tests (see CONCERNS: data:repository test broken)
- Fase 9 Polish — @Immutable states, Coil cache, proguard rules

**🚧 In progress (branch `feat/pokemon-compare-tlc-sdd`):** "Comparar Pokémon" feature, being built via
the `/tlc-spec-driven` skill (see `APOSTILA-SDD-pokemon-compare.md`). No `feature:pokemon-compare`
module exists yet.

**Brownfield analysis:** see `.specs/codebase/` (7 docs) and `.specs/codebase/CONCERNS.md`.

**Why:** Tracks where to resume in the next session without re-reading all files.

**How to apply:** Update this file at the end of each session.
