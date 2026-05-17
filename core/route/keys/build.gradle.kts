plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "br.com.pokedex.core.route.keys"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.runtime)
}
