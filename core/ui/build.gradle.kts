plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.pokedexlab.android.compose)
}

android {
    namespace = "br.com.pokedex.core.ui"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(project(":core:design-system"))
    implementation(project(":core:common"))
    debugImplementation(libs.bundles.compose.debug)
}
