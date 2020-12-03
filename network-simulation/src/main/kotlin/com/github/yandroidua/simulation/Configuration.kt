package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.Mode
import com.github.yandroidua.simulation.models.SimulationConnection
import com.github.yandroidua.simulation.models.SimulationPath
import com.github.yandroidua.simulation.models.SimulationWorkstation
import kotlin.math.ceil

data class Configuration(
        var path: SimulationPath? = null,
        var mode: Mode = Mode.LOGICAL,
        var infoPacketSize: Int = 0,
        var sysPacketSize: Int = 0,
        var size: Int = 0,
        var connectionRnadomDelayRange: IntArray = DEFAULT_DELAY_RANGE,
        var connectionRandomHalfDuplexAdditionRange: IntArray = DEFAULT_HALF_DUPLEX_DELAY_RANGE,
        var satteliteRandomDelayRange: IntArray = DEFAULT_SATELLITE_DELAY_RANGE
) {

    companion object {
        val DEFAULT_HALF_DUPLEX_DELAY_RANGE = intArrayOf(2, 7)
        val DEFAULT_DELAY_RANGE = intArrayOf(0, 5)
        val DEFAULT_SATELLITE_DELAY_RANGE = intArrayOf(4, 10)
    }

    //todo here will be result of simulation after method [Simulation::simulate]
    var simulationResult: Any? = null

}