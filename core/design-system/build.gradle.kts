plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.pokedexlab.android.compose)
}

android {
    namespace = "br.com.pokedex.core.designsystem"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.coil.compose)
    debugImplementation(libs.bundles.compose.debug)
}
