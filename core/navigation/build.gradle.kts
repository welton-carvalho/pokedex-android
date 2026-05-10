plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "br.com.pokedex.core.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
