plugins {
    alias(libs.plugins.pokedexlab.android.library)
}

android {
    namespace = "br.com.pokedex.core.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.androidx.paging.runtime)
}
