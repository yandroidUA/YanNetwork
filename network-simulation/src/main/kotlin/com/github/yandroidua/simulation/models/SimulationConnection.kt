package com.github.yandroidua.simulation.models

data class SimulationConnection(
        val id: Int,
        val workstation1Id: Int,
        val workstation2Id: Int,
        val type: LineType,
        val weight: Int,
        val errorChance: Float
)