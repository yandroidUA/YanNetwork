package com.github.yandroidua.simulation.models

import com.github.yandroidua.simulation.models.packets.Packet

sealed class Event {

    data class TextEvent(val text: String) : Event()

    data class SendPacketsEvent(val packets: List<Packet>) : Event()

    data class ErrorEvent(val description: String) : Event()

}