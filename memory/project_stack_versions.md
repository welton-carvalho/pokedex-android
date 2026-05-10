---
name: PokedexLab stack and versions
description: Exact library versions and SDK config for the project
type: project
---

**SDK:** minSdk=26, targetSdk=36, compileSdk=37 (37 required by material3-adaptive-navigation3:1.3.0-beta01)

**Build:** Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10

**Key versions (libs.versions.toml):**
- composeBom = 2026.02.01
- koin = 4.2.1
- retrofit = 3.0.0
- coil = 3.4.0
- objectbox = 5.4.2
- paging = 3.3.6
- nav3Core = 1.1.1
- lifecycleViewmodelNav3 = 2.11.0-beta01
- material3AdaptiveNav3 = 1.3.0-beta01
- timber = 5.0.1
- chucker = 4.0.0
- junit5 = 5.11.0
- mockk = 1.13.12

**Why:** Pinning versions here avoids re-deriving them from the toml file. Cross-reference with `gradle/libs.versions.toml` for the authoritative source.

**How to apply:** When adding a new dependency, check this list first. Never introduce a second version of an already-present library.
