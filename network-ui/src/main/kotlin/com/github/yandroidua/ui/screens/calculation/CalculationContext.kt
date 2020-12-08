package com.github.yandroidua.ui.screens.calculation

import androidx.compose.runtime.MutableState
import com.github.yandroidua.simulation.models.Mode
import com.github.yandroidua.ui.elements.base.ConnectableElement

data class CalculationContext(
    val fromWorkstationState: MutableState<ConnectableElement?>,
    val toWorkstationState: MutableState<ConnectableElement?>,
    val workstationFromDropDownState: MutableState<Boolean>,
    val workstationToDropDownState: MutableState<Boolean>,
    val messageSizeState: MutableState<String>,
    val tcpHeaderState: MutableState<String>,
    val udpHeaderSizeState: MutableState<String>,
    val infoPacketSizeState: MutableState<String>,
    val simulationModeState: MutableState<Mode>,
    val calculationFromErrorContext: CalculationFromErrorContext
)