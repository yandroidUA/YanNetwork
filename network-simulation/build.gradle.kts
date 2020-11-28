plugins {
    kotlin("jvm") version "1.4.10"
}

group = "com.github.yandroidua"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
}
