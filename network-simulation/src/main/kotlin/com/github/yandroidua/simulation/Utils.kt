package com.github.yandroidua.simulation

fun buildConfiguration(builder: Configuration.() -> Unit): Configuration {
    return Configuration().apply(builder)
}
