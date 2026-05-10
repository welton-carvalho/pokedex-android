plugins {
    alias(libs.plugins.pokedexlab.android.library)
}

android {
    namespace = "br.com.pokedex.core.observability"
}

dependencies {
    implementation(libs.timber)
    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.no.op)
}
