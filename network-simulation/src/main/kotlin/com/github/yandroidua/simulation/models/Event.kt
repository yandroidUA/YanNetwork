package com.github.yandroidua.simulation.models

import com.github.yandroidua.simulation.models.packets.Packet

sealed class Event {

    data class TextEvent(val text: String) : Event()

    data class SendPacketEvent(
            val fromStationId: Int,
            val toStationId: Int,
            val lineId: Int,
            val time: Long,
            val packet: Packet
    ) : Event()

    object EndSimulationEvent: Event()

    data class ErrorEvent(val description: String) : Event()

}