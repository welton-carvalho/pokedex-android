plugins {
    alias(libs.plugins.pokedexlab.android.library)
}

android {
    namespace = "br.com.pokedex.core.route.deeplink"
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
    implementation(project(":core:route:keys"))
}
