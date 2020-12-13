package com.github.yandroidua.ui.mappers

import com.github.yandroidua.simulation.models.Event
import com.github.yandroidua.ui.models.SimulationResultModel

fun Event.SendPacketEvent.mapToUiEvent(): SimulationResultModel =
    SimulationResultModel.MessageStartModel(packet.id, packet.type, packet.size, fromStationId, toStationId, lineId, time)

fun Event.EndSimulationEvent.mapToUiEvent(): SimulationResultModel =
    SimulationResultModel.EndSimulation(systemTraffic, informationTraffic, systemPackages, infoPackages)

fun Event.mapToUiEvent(): SimulationResultModel {
    return when (this) {
        is Event.SendPacketEvent -> this.mapToUiEvent()
        is Event.EndSimulationEvent -> this.mapToUiEvent()
    }
}