import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // kotlin.plugin.compose works alongside kotlin.android for library modules
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.findByType<ApplicationExtension>()?.configureCompose()
            extensions.findByType<LibraryExtension>()?.configureCompose()
        }
    }
}
