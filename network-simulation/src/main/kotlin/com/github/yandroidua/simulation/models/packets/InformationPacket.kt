package com.github.yandroidua.simulation.models.packets

data class InformationPacket(
   override val id: Int,
   override val size: Int
) : Packet {
   override val type: PacketType = PacketType.INFORMATION
}