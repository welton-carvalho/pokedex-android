import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinAndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9.x manages Kotlin internally; configure compiler options only
            configureKotlinCompiler()
        }
    }
}
