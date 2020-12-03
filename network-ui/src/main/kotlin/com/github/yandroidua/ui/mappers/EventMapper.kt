package com.github.yandroidua.ui.mappers

import androidx.compose.ui.geometry.Offset
import com.github.yandroidua.simulation.models.Event
import com.github.yandroidua.ui.models.SimulationResultModel

fun Event.ErrorEvent.mapToUiEvent(): SimulationResultModel.ErrorMessageModel =  SimulationResultModel.ErrorMessageModel(
        description
)

fun Event.TextEvent.mapToUiEvent(): SimulationResultModel.TextSimulationModel = SimulationResultModel.TextSimulationModel(
        text
)

fun Event.SendPacketsEvent.mapToUiEvent(): SimulationResultModel =
    SimulationResultModel.MessageStartModel(fromStationId, toStationId, lineId, time)


fun Event.mapToUiEvent(): SimulationResultModel {
    return when (this) {
        is Event.TextEvent -> this.mapToUiEvent()
        is Event.SendPacketsEvent -> this.mapToUiEvent()
        is Event.ErrorEvent -> this.mapToUiEvent()
    }
}