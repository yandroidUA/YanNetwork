package com.github.yandroidua.ui.models

import androidx.compose.ui.geometry.Offset
import com.github.yandroidua.simulation.models.packets.PacketType

sealed class SimulationResultModel {

   data class TextSimulationModel(val text: String) : SimulationResultModel()

   data class MessageStartModel(
      val packetId: Int,
      val packetType: PacketType,
      val size: Int,
      val from: Int,
      val to: Int,
      val by: Int,
      val time: Long
   ) : SimulationResultModel()

   data class MessageMoveModel(
      val packetId: Int,
      val packetType: PacketType,
      val from: Int,
      val to: Int,
      val by: Int,
      val offset: Offset,
      val time: Long
      ) : SimulationResultModel()

   data class EndSimulation(val systemBytes: Int, val infoBytes: Int) : SimulationResultModel()

   data class ErrorMessageModel(val reason: String) : SimulationResultModel()

}