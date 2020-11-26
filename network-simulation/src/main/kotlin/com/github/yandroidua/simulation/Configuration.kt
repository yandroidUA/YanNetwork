package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.SimulationConnection
import com.github.yandroidua.simulation.models.SimulationWorkstation

data class Configuration(
    var connections: List<SimulationConnection> = emptyList(),
    var workstations: List<SimulationWorkstation> = emptyList(),
    var connectionRnadomDelayRange: IntArray = DEFAULT_DELAY_RANGE,
    var connectionRandomHalfDuplexAdditionRange: IntArray = DEFAULT_HALF_DUPLEX_DELAY_RANGE,
    var satteliteRandomDelayRange: IntArray = DEFAULT_SATELLITE_DELAY_RANGE
) {

    //todo create path model
    //todo add path to Configuration

    companion object {
        val DEFAULT_HALF_DUPLEX_DELAY_RANGE = intArrayOf(2, 7)
        val DEFAULT_DELAY_RANGE = intArrayOf(0, 5)
        val DEFAULT_SATELLITE_DELAY_RANGE = intArrayOf(4, 10)
    }

}