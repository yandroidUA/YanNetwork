package com.github.yandroidua.simulation.models.packets

interface Packet {
    val size: Int
    val type: PacketType
}