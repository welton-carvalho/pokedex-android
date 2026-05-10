plugins {
    alias(libs.plugins.pokedexlab.android.library)
}

android {
    namespace = "br.com.pokedex.data.local"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.koin.android)
}
