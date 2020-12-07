package com.github.yandroidua.simulation.models

import com.github.yandroidua.simulation.models.packets.Packet

sealed class Event {

    data class SendPacketEvent(
            val fromStationId: Int,
            val toStationId: Int,
            val lineId: Int,
            val time: Long,
            val packet: Packet
    ) : Event()

    data class EndSimulationEvent(
       val systemTraffic: Int,
       val informationTraffic: Int
    ): Event()

}