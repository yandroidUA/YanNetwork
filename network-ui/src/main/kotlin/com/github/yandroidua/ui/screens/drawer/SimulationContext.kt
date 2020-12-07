package com.github.yandroidua.ui.screens.drawer

import androidx.compose.runtime.MutableState
import com.github.yandroidua.simulation.models.Mode
import com.github.yandroidua.ui.models.PathResultElements
import com.github.yandroidua.ui.models.SimulationResultModel
import kotlinx.coroutines.Job

data class SimulationContext(
    val simulationStartedState: MutableState<Boolean>,
    val simulationStoppedState: MutableState<Boolean>,
    val simulationPathState: MutableState<PathResultElements?>,
    var startTime: Long = 0,
    var endTime: Long = 0,
    var fromId: Int = 0,
    var toId: Int = 0,
    var infoPacketSize: Int = 256,
    var sysPacketSize: Int = 10,
    var frameSysSize: Int = 10,
    var size: Int = 65536,
    var resultInfoSize: Int = 0,
    var resultSysSize: Int = 0,
    var mode: Mode = Mode.LOGICAL,
    var next: Boolean = false,
    var events: List<SimulationResultModel> = emptyList(),
    var simulationStarted: Boolean = false
)