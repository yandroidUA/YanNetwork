package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.*
import com.github.yandroidua.simulation.models.packets.InformationPacket
import com.github.yandroidua.simulation.models.packets.Packet
import com.github.yandroidua.simulation.models.packets.SystemInformationPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

class Simulation(
   private val configuration: Configuration,
   private val models: List<SimulationModel>
) {

   companion object {
      private const val LOGIC_ADDITIONAL_PACKAGES_COUNT = 2 //for connection
//      const val INFO_EMITS_PER_SEND = 1 // emits Before Event.SendPacketsEvent
   }

   private fun calculateInformationPackages(connection: SimulationConnection): Int {
      return ceil((1f + connection.errorChance) * configuration.size / configuration.sysPacketSize).toInt()
   }

   private fun calculateSystemInformationPackages(connection: SimulationConnection): Int {
      return if (configuration.mode == Mode.LOGICAL) {
         LOGIC_ADDITIONAL_PACKAGES_COUNT
      } else {
         0
      } + calculateInformationPackages(connection)
   }

   private fun calculateDelay(connection: SimulationConnection): Long {
      return 200
   }

   private fun sendPackageToEnd(
      scope: CoroutineScope,
      packet: Packet,
      path: SimulationPath,
      handler: suspend (Event) -> Unit,
      after: suspend () -> Unit = {}
   ) = scope.launch {
      var fromWorkstation: SimulationWorkstation =
         models.find { it.id == path.from } as? SimulationWorkstation ?: return@launch

      for ((index, pathEntry) in path.path.withIndex()) {
         if (index == 0) continue

         val connection = models.find { it.id == pathEntry.first } as? SimulationConnection ?: return@launch
         val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation ?: return@launch

         sendPackage(fromWorkstation.id, workstation.id, connection.id, packet, 100L, handler)
         fromWorkstation = workstation
      }
      after()
   }

   private suspend fun sendPackageToEndSuspended(
      packet: Packet,
      path: SimulationPath,
      handler: suspend (Event) -> Unit
   ) {
      var fromWorkstation: SimulationWorkstation =
         models.find { it.id == path.from } as? SimulationWorkstation ?: return

      for ((index, pathEntry) in path.path.withIndex()) {
         if (index == 0) continue

         val connection = models.find { it.id == pathEntry.first } as? SimulationConnection ?: return
         val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation ?: return

         sendPackage(fromWorkstation.id, workstation.id, connection.id, packet, 100L, handler)
         fromWorkstation = workstation
      }
   }

   private suspend fun sendPackage(
      from: Int,
      to: Int,
      by: Int,
      packet: Packet,
      time: Long,
      handler: suspend (Event) -> Unit
   ) {
      handler(
         Event.SendPacketEvent(
            fromStationId = from,
            toStationId = to,
            lineId = by,
            packet = packet,
            time = time
         )
      )
   }

   fun simulate(
      scope: CoroutineScope,
      idGenerator: suspend () -> Int,
      handler: suspend (Event) -> Unit
   ) = scope.launch {
      val path = configuration.path ?: return@launch


//      TCP / UDP
      repeat(100) {
         sendPackageToEnd(scope, InformationPacket(id = idGenerator(), size = 20), path, handler) {
            sendPackageToEnd(scope, SystemInformationPacket(id = idGenerator(), size = 20), path.reverse(), handler)
         }
         delay(200L)
      }

      //TCP
//      repeat(3) {
//         sendPackageToEndSuspended(InformationPacket(id = idGenerator(), size = 20), path, handler)
//         sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = 20), path.reverse(), handler)
//         delay(200L)
//      }

   }

}