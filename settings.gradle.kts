rootProject.name = "YanNetwork"
include("network-ui")
include("network-algorithm")

pluginManagement {
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
    }
}
include("network-dump")
include("network-simulation")
