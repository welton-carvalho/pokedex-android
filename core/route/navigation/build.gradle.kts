plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.pokedexlab.android.compose)
}

android {
    namespace = "br.com.pokedex.core.route.navigation"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.navigation3)

    implementation(project(":core:route:keys"))
    implementation(project(":core:route:deeplink"))
    implementation(project(":feature:pokemon-list"))
    implementation(project(":feature:pokemon-detail"))
}
