package com.github.yandroidua.ui.screens.drawer

import androidx.compose.runtime.MutableState
import com.github.yandroidua.simulation.models.Mode
import com.github.yandroidua.ui.models.PathResultElements
import kotlinx.coroutines.Job

data class SimulationContext(
    val simulationStartedState: MutableState<Boolean>,
    var infoPacketSize: Int = 256,
    var sysPacketSize: Int = 10,
    var size: Int = 65536,
    var mode: Mode = Mode.LOGICAL,
    var simulationJob: Job? = null,
    var simulationPath: PathResultElements? = null,
    var currentStep: Int = 0,
    var simulationStartStep: Int = 0,
    var simulationStarted: Boolean = false
)