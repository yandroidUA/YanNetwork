package com.github.yandroidua.simulation.models

data class SimulationWorkstation(
        override val id: Int,
        val network: String?,
        val connectionIds: List<Int>,
        val isWorkstation: Boolean = false
) : SimulationModel