import org.jetbrains.compose.compose

plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.compose") version "0.3.0-build134"
    application
}

group = "com.github.yandroidua"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":network-algorithm"))
    implementation(project(":network-dump"))
    implementation(project(":network-simulation"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation(compose.desktop.currentOs)
}

application {
    mainClassName = "com.github.yandroidua.ui.AppKt"
}

