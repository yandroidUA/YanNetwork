package com.github.yandroidua.simulation.models

data class SimulationWorkstation(
        override val id: Int,
        val connectionIds: List<Int>
) : SimulationModel