pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Pokedex Lab"

include(":app")

// Core modules
include(":core:common")
include(":core:design-system")
include(":core:domain")
include(":core:model")
include(":core:observability")
include(":core:testing")
include(":core:ui")

// Route modules
include(":core:route:keys")
include(":core:route:deeplink")
include(":core:route:navigation")

// Data modules
include(":data:network")
include(":data:local")
include(":data:repository")

// Feature modules
include(":feature:pokemon-list")
include(":feature:pokemon-detail")
