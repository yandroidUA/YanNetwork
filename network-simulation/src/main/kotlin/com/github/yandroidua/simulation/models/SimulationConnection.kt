package com.github.yandroidua.simulation.models

data class SimulationConnection(
        override val id: Int,
        val workstation1Id: Int,
        val workstation2Id: Int,
        val type: LineType,
        val weight: Int,
        val errorChance: Float
) : SimulationModel