package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.*
import com.github.yandroidua.simulation.models.packets.InformationPacket
import com.github.yandroidua.simulation.models.packets.Packet
import com.github.yandroidua.simulation.models.packets.PacketType
import com.github.yandroidua.simulation.models.packets.SystemInformationPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.math.ceil

class Simulation(
   private val configuration: Configuration,
   private val models: List<SimulationModel>
) {

   companion object {
      private const val LOGIC_ADDITIONAL_PACKAGES_COUNT = 2 //for connection
//      const val INFO_EMITS_PER_SEND = 1 // emits Before Event.SendPacketsEvent
   }

   private var systemPackagesBytes: Int = 0
   private var infoPackagesBytes: Int = 0
   private val counterMutex = Mutex(locked = false)

   private fun calculateInformationPackages(): Int {
      return ceil(configuration.size.toFloat() / configuration.infoPacketSize).toInt()
   }
//
//   private fun calculateSystemInformationPackages(connection: SimulationConnection): Int {
//      return if (configuration.mode == Mode.LOGICAL) {
//         LOGIC_ADDITIONAL_PACKAGES_COUNT
//      } else {
//         0
//      } + calculateInformationPackages(connection)
//   }

   private fun calculateDelay(connection: SimulationConnection): Long {
      return 200
   }

   private fun sendPackageToEnd(
      scope: CoroutineScope,
      packet: Packet,
      path: SimulationPath,
      handler: suspend (Event, Boolean) -> Boolean,
      useError: Boolean,
      after: suspend () -> Unit = {}
   ) = scope.launch {
      var fromWorkstation: SimulationWorkstation =
         models.find { it.id == path.from } as? SimulationWorkstation ?: return@launch
//      addPackageSize(packet)
      for ((index, pathEntry) in path.path.withIndex()) {
         if (index == 0) continue

         val connection = models.find { it.id == pathEntry.first } as? SimulationConnection ?: return@launch
         val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation ?: return@launch

         sendPackage(fromWorkstation.id, workstation.id, connection, packet, 100L, useError, handler)
         fromWorkstation = workstation
      }
      after()
   }

   private suspend fun sendPackageToEndSuspended(
      packet: Packet,
      path: SimulationPath,
      useError: Boolean,
      handler: suspend (Event, Boolean) -> Boolean
   ): Boolean {
      var fromWorkstation: SimulationWorkstation =
         models.find { it.id == path.from } as? SimulationWorkstation ?: return false
//      addPackageSize(packet)
      for ((index, pathEntry) in path.path.withIndex()) {
         if (index == 0) continue

         val connection = models.find { it.id == pathEntry.first } as? SimulationConnection ?: return false
         val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation ?: return false

         if (!sendPackage(fromWorkstation.id, workstation.id, connection, packet, 100L, useError, handler)) {
            sendPackageToEndSuspended(packet, path, useError, handler)
            break
         }
         fromWorkstation = workstation
      }
      return true
   }

   private suspend fun addPackageSize(packet: Packet) {
      counterMutex.lock()
      when (packet.type) {
         PacketType.INFORMATION -> {
            infoPackagesBytes += packet.size
         }
         PacketType.SYSTEM -> {
            systemPackagesBytes += packet.size
         }
         PacketType.ERROR ->{}
      }
      counterMutex.unlock()
   }

   private suspend fun sendPackage(
      from: Int,
      to: Int,
      by: SimulationConnection,
      packet: Packet,
      time: Long,
      useError: Boolean,
      handler: suspend (Event, Boolean) -> Boolean
   ): Boolean {
      return handler(
            Event.SendPacketEvent(
               fromStationId = from,
               toStationId = to,
               lineId = by.id,
               packet = packet,
               time = time
            ),
            useError
         )
   }

   fun simulate(
      scope: CoroutineScope,
      idGenerator: suspend () -> Int,
      handler: suspend (Event, Boolean) -> Boolean
   ) = scope.launch {
      val path = configuration.path ?: return@launch
      when (configuration.mode) {
         Mode.LOGICAL -> simulateTCP(path, idGenerator, handler)
         Mode.DATAGRAM -> simulateUDP(scope, path, idGenerator, handler)
      }
   }

   private suspend fun simulateUDP(
      scope: CoroutineScope,
      path: SimulationPath,
      idGenerator: suspend () -> Int,
      handler: suspend (Event, Boolean) -> Boolean
   ) {
      val packetCount = calculateInformationPackages()
      repeat(packetCount) {
         val packetId = idGenerator()
         sendPackageToEnd(scope, InformationPacket(id = packetId, size = 20), path, handler, true)
         delay(100L)
      }
      handler(Event.EndSimulationEvent(systemPackagesBytes, infoPackagesBytes), false)
      println("Simulate UDP")
   }

   private suspend fun simulateTCP(
      path: SimulationPath,
      idGenerator: suspend () -> Int,
      handler: suspend (Event, Boolean) -> Boolean
   ) {
      val packetCount = calculateInformationPackages()
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path, false, handler)
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path.reverse(), false, handler)
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path, false, handler)
      repeat(packetCount) {
         val packetId = idGenerator()
         if (sendPackageToEndSuspended(InformationPacket(id = packetId, size = 20), path, true, handler)) {
            //todo here handle HALF_DUPLEX DELAY
            sendPackageToEndSuspended(SystemInformationPacket(id = packetId, size = configuration.frameInformationSize + configuration.sysPacketSize), path.reverse(), false, handler)
         }
      }
      // FIN
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path, false, handler)
      // ACK
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path.reverse(), false, handler)
      // FIN
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path.reverse(), false, handler)
      // ACK
      sendPackageToEndSuspended(SystemInformationPacket(id = idGenerator(), size = configuration.frameInformationSize + configuration.sysPacketSize), path, false, handler)
      handler(Event.EndSimulationEvent(systemPackagesBytes, infoPackagesBytes), false)
   }

}