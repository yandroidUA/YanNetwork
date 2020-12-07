package com.github.yandroidua.ui.mappers

import com.github.yandroidua.simulation.models.Event
import com.github.yandroidua.ui.models.SimulationResultModel

fun Event.SendPacketEvent.mapToUiEvent(): SimulationResultModel =
    SimulationResultModel.MessageStartModel(packet.id, packet.type, fromStationId, toStationId, lineId, packet.size, time)


fun Event.mapToUiEvent(): SimulationResultModel {
    return when (this) {
        is Event.SendPacketEvent -> this.mapToUiEvent()
        is Event.EndSimulationEvent -> SimulationResultModel.EndSimulation
    }
}