rootProject.name = "YanNetwork"
include("network-ui")

pluginManagement {
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
    }
//    plugins {
//        kotlin("jvm")
//    }
}
