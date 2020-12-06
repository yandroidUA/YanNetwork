package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.SimulationPath

fun buildConfiguration(builder: Configuration.() -> Unit): Configuration {
    return Configuration().apply(builder)
}

fun SimulationPath.reverse(): SimulationPath {
    return this.copy(from = this.to, to = this.from, path = this.path.asReversed())
}
