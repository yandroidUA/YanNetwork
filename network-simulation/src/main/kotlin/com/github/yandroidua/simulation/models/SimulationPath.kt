package com.github.yandroidua.simulation.models

data class SimulationPath(
        val from: WorkstationId,
        val to: WorkstationId,
        val summaryWeight: Int,
        val path: List<Pair<ConnectionId, WorkstationId>>
)