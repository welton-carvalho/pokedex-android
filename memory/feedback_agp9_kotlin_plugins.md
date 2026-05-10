---
name: AGP 9.x Kotlin plugin compatibility
description: How to apply Kotlin plugins in convention plugins with AGP 9.x + Kotlin 2.x
type: feedback
---

AGP 9.x registers the 'kotlin' extension internally for ALL Android modules (application and library). Applying `org.jetbrains.kotlin.android` explicitly throws "Cannot add extension with name 'kotlin', as there is an extension already registered".

**Rule:** Never apply `org.jetbrains.kotlin.android` in convention plugins when using AGP 9.x.

**Correct pattern:**
- Non-Compose library: `com.android.library` only (AGP handles Kotlin)
- Compose library: `com.android.library` + `org.jetbrains.kotlin.plugin.compose`
- Application: `com.android.application` + `org.jetbrains.kotlin.plugin.compose`

**Why:** AGP 9.x has built-in Kotlin support. The original project template used only `kotlin.plugin.compose` (no `kotlin.android`), which was the hint.

**Also:** `material3-adaptive-navigation3:1.3.0-beta01` requires `compileSdk = 37`, not 36.

**How to apply:** Use this pattern in any future Android multi-module projects with AGP 9.x.
