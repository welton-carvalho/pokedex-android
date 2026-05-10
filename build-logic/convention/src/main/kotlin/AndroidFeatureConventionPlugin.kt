import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("pokedexlab.android.library")
                apply("pokedexlab.android.compose")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
