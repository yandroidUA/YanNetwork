package com.github.yandroidua.simulation.models.packets

interface Packet {
    val id: Int
    val size: Int
    val type: PacketType
}