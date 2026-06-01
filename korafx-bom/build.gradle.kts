import org.gradle.api.plugins.JavaPlatformExtension

extensions.configure<JavaPlatformExtension>("javaPlatform") {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":korafx-dsl"))
        api(project(":korafx-navigation"))
        api(project(":korafx-framework"))
        api(project(":korafx-command-palette"))
        api(project(":korafx-components"))
        api(project(":korafx-data-grid"))
        api(project(":korafx-graph-editor"))
        api(project(":korafx-inspector-panel"))
        api(project(":korafx-resource-explorer"))
        api(project(":korafx-virtual-list"))
        api(project(":korafx-source-editor"))
        api(project(":korafx-test"))
        api(project(":korafx-devtools"))
        api(project(":korafx-macos"))

        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.coroutines.javafx)
        api(libs.kotlinx.coroutines.test)
        api(libs.koin.core) {
            version {
                require(libs.versions.koin.bom.get())
            }
        }
        api(libs.ikonli.javafx)
        api(libs.ikonli.bootstrapicons.pack)
        api(libs.caffeine)
        api(libs.testfx.core)
        api(libs.testfx.junit5)
        api(libs.kotlin.test.junit5) {
            version {
                require(libs.versions.kotlin.get())
            }
        }
    }
}
