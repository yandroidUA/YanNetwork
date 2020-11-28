package com.github.yandroidua.simulation

fun buildConfiguration(builder: Configuration.() -> Unit): Configuration {
    return Configuration().apply(builder)
}

fun test() {
//    val config = buildConfiguration {
//        workstations = listOf()
//        connections = listOf()
//    }
}
