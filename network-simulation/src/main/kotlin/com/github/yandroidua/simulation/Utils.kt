package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.SimulationConnection
import com.github.yandroidua.simulation.models.SimulationPath
import com.github.yandroidua.simulation.models.WorkstationId

fun buildConfiguration(builder: Configuration.() -> Unit): Configuration {
    return Configuration().apply(builder)
}

fun SimulationPath.reverse(): SimulationPath {
    return this.copy(from = this.to, to = this.from, path = this.path.asReversed())
}

fun SimulationConnection.anotherWorkstation(workstationId: WorkstationId): WorkstationId {
    return if (workstation1Id == workstationId) workstation2Id else workstation1Id
}
