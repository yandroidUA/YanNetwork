package com.github.yandroidua.simulation.models

sealed class SimulationParams {

   data class TcpSimulationParams(
      val path: SimulationPath?,
      val idGenerator: suspend () -> Int,
      val handler: suspend (Event, Boolean) -> Boolean
   ) : SimulationParams()

   data class UdpSimulationParams(
      val from: Int,
      val to: Int,
      val routingTables: List<SimulationRoutingTable>,
      val idGenerator: suspend () -> Int,
      val handler: suspend (Event, Boolean) -> Boolean
   ): SimulationParams()

}
