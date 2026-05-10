import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "br.com.pokedex.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.kotlin.android.toDep())
    compileOnly(libs.plugins.kotlin.compose.toDep())
    compileOnly(libs.plugins.kotlin.serialization.toDep())
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "pokedexlab.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "pokedexlab.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "pokedexlab.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "pokedexlab.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("kotlinAndroid") {
            id = "pokedexlab.kotlin.android"
            implementationClass = "KotlinAndroidConventionPlugin"
        }
    }
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
