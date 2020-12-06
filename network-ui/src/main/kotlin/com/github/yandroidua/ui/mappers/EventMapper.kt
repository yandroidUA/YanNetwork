package com.github.yandroidua.ui.mappers

import com.github.yandroidua.simulation.models.Event
import com.github.yandroidua.ui.models.SimulationResultModel

fun Event.ErrorEvent.mapToUiEvent(): SimulationResultModel.ErrorMessageModel =  SimulationResultModel.ErrorMessageModel(
        description
)

fun Event.TextEvent.mapToUiEvent(): SimulationResultModel.TextSimulationModel = SimulationResultModel.TextSimulationModel(
        text
)

fun Event.SendPacketEvent.mapToUiEvent(): SimulationResultModel =
    SimulationResultModel.MessageStartModel(packet.id, packet.type, fromStationId, toStationId, lineId, time)


fun Event.mapToUiEvent(): SimulationResultModel {
    return when (this) {
        is Event.TextEvent -> this.mapToUiEvent()
        is Event.SendPacketEvent -> this.mapToUiEvent()
        is Event.ErrorEvent -> this.mapToUiEvent()
        is Event.EndSimulationEvent -> SimulationResultModel.EndSimulation
    }
}