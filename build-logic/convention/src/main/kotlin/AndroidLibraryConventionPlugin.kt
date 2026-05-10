import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9.x manages Kotlin compilation internally — no explicit kotlin.android plugin needed
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid()
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
            }

            configureKotlinCompiler()
        }
    }
}
