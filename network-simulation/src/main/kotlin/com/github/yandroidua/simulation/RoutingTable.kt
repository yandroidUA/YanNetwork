package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.*

class RoutingTable(
   private val allElements: List<SimulationModel>,
   private val workstation: SimulationWorkstation
) {

   fun routingTable(pathRequester: (from: WorkstationId, to: WorkstationId) -> SimulationPath?): List<SimulationRoutingTableEntry> {
      val result = mutableListOf<SimulationRoutingTableEntry>()

      // if workstation not in network it should know about self and neighbours
      result.add(SimulationRoutingTableEntry(toId = workstation.id, fromId = null))

      for (connectionId in workstation.connectionIds) {
         val line = allElements.find { it.id == connectionId } as? SimulationConnection ?: continue
         val neighbourId = if (line.workstation1Id == workstation.id) line.workstation2Id else line.workstation1Id
         result.add(SimulationRoutingTableEntry(toId = neighbourId, fromId = neighbourId))
      }
      // workstation not consist in any network, so it knows only about it's neighbours
      if (workstation.network.isNullOrBlank()) return result

      // scan network except neighbours
      val networkElements = allElements.asSequence()
         .filterIsInstance<SimulationWorkstation>()
         .filter { w -> w.network == workstation.network && !result.any { it.toId == w.id } }

      for (networkElement in networkElements) {
         val optimalPath = pathRequester(workstation.id, networkElement.id) ?: continue
         val nextElement = optimalPath.path.getOrNull(1) ?: continue
         result.add(SimulationRoutingTableEntry(toId = networkElement.id, nextElement.second))
      }
      return result
   }

}