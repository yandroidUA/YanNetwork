package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.Mode
import com.github.yandroidua.simulation.models.SimulationPath

data class Configuration(
        var path: SimulationPath? = null,
        var mode: Mode = Mode.LOGICAL,
        var infoPacketSize: Int = 0,
        var tcpHeaderSize: Int = 16,
        var size: Int = 0,
        var udpHeaderSize: Int = 8
)