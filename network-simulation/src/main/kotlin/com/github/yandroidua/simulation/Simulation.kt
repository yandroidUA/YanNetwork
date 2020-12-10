package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.*
import com.github.yandroidua.simulation.models.packets.InformationPacket
import com.github.yandroidua.simulation.models.packets.Packet
import com.github.yandroidua.simulation.models.packets.PacketType
import com.github.yandroidua.simulation.models.packets.SystemInformationPacket
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
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
   private val connectionInformation = ConnectionInformation(models.filterIsInstance<SimulationConnection>())

   private fun findConnection(connectionId: ConnectionId): SimulationConnection? {
      return models.find { it.id == connectionId } as? SimulationConnection
   }

   private fun findWorkstation(workstationId: WorkstationId): SimulationWorkstation? {
      return models.find { it.id == workstationId } as? SimulationWorkstation
   }

   private fun calculateInformationPackages(): Int {
      return ceil(configuration.size.toFloat() / configuration.infoPacketSize).toInt()
   }

   private fun calculateDelay(connection: SimulationConnection): Long {
      return 50L * if (connection.type == LineType.HALF_DUPLEX) 2 else 1
   }

   private suspend fun onCannotSendByRoutingTable(
      scope: CoroutineScope,
      currentWorkstationId: WorkstationId,
      toId: WorkstationId,
      routingTables: List<SimulationRoutingTable>,
      packet: Packet,
      handler: suspend (Event, Boolean) -> Boolean
   ): Job {
      val currentWorkstation = findWorkstation(currentWorkstationId) ?: return Job()
      val neighbourWorkstationId = tryFindNeighbourInSameNetwork(currentWorkstation) ?: return Job()
      val line = findLineThatConnect(currentWorkstationId, neighbourWorkstationId) ?: return Job()
      sendPackage(
         from = currentWorkstationId,
         to = neighbourWorkstationId,
         line,
         packet,
         calculateDelay(line),
         true,
         handler
      )
      return sendPackageToEndWithRoutingTable(scope, neighbourWorkstationId, toId, routingTables, packet, handler, true)
   }

   private fun sendPackageToEndWithRoutingTable(
      scope: CoroutineScope,
      fromId: Int,
      toId: Int,
      routingTables: List<SimulationRoutingTable>,
      packet: Packet,
      handler: suspend (Event, Boolean) -> Boolean,
      isFromError: Boolean = false
   ): Job = scope.launch {
      var fromWorkstationId = fromId
      val toWorkstation: SimulationWorkstation =
         models.find { it.id == toId } as? SimulationWorkstation ?: return@launch
      var nextWorkstationId: Int

      if (!isFromError) {
         addPackageSize(packet)
      }

      while (fromWorkstationId != toWorkstation.id) {
         val routingTable = routingTables.find { it.id == fromWorkstationId } ?: return@launch //todo send error
         nextWorkstationId =
            routingTable.entries.find { it.toId == toWorkstation.id }?.fromId ?: return@launch //todo send error
         val line = findLineThatConnect(fromWorkstationId, nextWorkstationId) ?: return@launch //todo send error

         if (!connectionInformation.trySend(line.id)) {
            println("Cannot send by this line")
            onCannotSendByRoutingTable(scope, fromId, toId, routingTables, packet, handler).join()
            return@launch
         }

         if (!sendPackage(
               from = fromWorkstationId,
               to = nextWorkstationId,
               line,
               packet,
               calculateDelay(line),
               true,
               handler
            )
         )
            return@launch

         connectionInformation.release(line.id)
         fromWorkstationId = nextWorkstationId
      }
   }

   private fun tryFindNeighbourInSameNetwork(workstation: SimulationWorkstation): WorkstationId? {
      var cacheId: WorkstationId? = null
      for (connectionId in workstation.connectionIds) {
         val line = findConnection(connectionId) ?: continue
         val w = findWorkstation(line.anotherWorkstation(workstationId = workstation.id))
         if (w?.network == workstation.network && w?.id != workstation.id) return w?.id else cacheId = w?.id
      }
      return cacheId
   }

   private fun findLineThatConnect(one: Int, two: Int): SimulationConnection? {
      return models.find {
         it is SimulationConnection
             && (
             (it.workstation1Id == one && it.workstation2Id == two)
                 || (it.workstation2Id == one && it.workstation1Id == two)
             )
      } as? SimulationConnection
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
      addPackageSize(packet)
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
      addPackageSize(packet)
      for ((index, pathEntry) in path.path.withIndex()) {
         if (index == 0) continue

         val connection = models.find { it.id == pathEntry.first } as? SimulationConnection ?: return false
         val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation ?: return false

         if (!sendPackage(
               fromWorkstation.id,
               workstation.id,
               connection,
               packet,
               calculateDelay(connection),
               useError,
               handler
            )
         ) {
            return sendPackageToEndSuspended(packet, path, useError, handler)
         }
         fromWorkstation = workstation
      }
      return true
   }

   private suspend fun addPackageSize(packet: Packet, isUDP: Boolean = false) {
      counterMutex.lock()
      when (packet.type) {
         PacketType.INFORMATION -> {
            println("Adding ${packet.size} to INFO TRAFFIC")
            infoPackagesBytes += packet.size
            systemPackagesBytes += if (isUDP) {
               configuration.udpHeaderSize
            } else {
               configuration.tcpHeaderSize
            }
         }
         PacketType.SYSTEM -> {
            println("Adding ${packet.size} to SYS TRAFFIC")
            systemPackagesBytes += packet.size
         }
         PacketType.ERROR -> {
         }
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

   fun simulate(scope: CoroutineScope, params: SimulationParams) = scope.launch {
      when (params) {
         is SimulationParams.TcpSimulationParams -> startTcpSimulation(scope, params)
         is SimulationParams.UdpSimulationParams -> startUdpSimulation(scope, params)
      }
   }

   private suspend fun startTcpSimulation(scope: CoroutineScope, param: SimulationParams.TcpSimulationParams) {
      param.path ?: return
      simulateTCP(path = param.path, idGenerator = param.idGenerator, handler = param.handler)
   }

   private suspend fun startUdpSimulation(scope: CoroutineScope, param: SimulationParams.UdpSimulationParams) {
      simulateUDP(scope, param.from, param.to, param.routingTables, param.idGenerator, param.handler)
   }

   private suspend fun simulateUDP(
      scope: CoroutineScope,
      from: Int,
      to: Int,
      routingTables: List<SimulationRoutingTable>,
      idGenerator: suspend () -> Int,
      handler: suspend (Event, Boolean) -> Boolean
   ) {
      val packetCount = calculateInformationPackages()
      val packageJobs = mutableListOf<Job>()
      repeat(packetCount) {
         val packetId = idGenerator()
         packageJobs.add(
            sendPackageToEndWithRoutingTable(
               scope,
               from,
               to,
               routingTables,
               InformationPacket(id = packetId, size = configuration.infoPacketSize),
               handler
            )
         )
//         sendPackageToEnd(scope, InformationPacket(id = packetId, size = 20), path, handler, true)
         delay(1L)
      }
      packageJobs.joinAll()
      handler(Event.EndSimulationEvent(systemPackagesBytes, infoPackagesBytes), false)
   }

   private suspend fun simulateTCP(
      path: SimulationPath,
      idGenerator: suspend () -> Int,
      handler: suspend (Event, Boolean) -> Boolean
   ) {
      val packetCount = calculateInformationPackages()
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path, false, handler
      )
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path.reverse(), false, handler
      )
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path, false, handler
      )
      repeat(packetCount) {
         val packetId = idGenerator()
         if (sendPackageToEndSuspended(
               InformationPacket(
                  id = packetId,
                  size = configuration.infoPacketSize
               ), path, true, handler
            )
         ) {
            sendPackageToEndSuspended(
               SystemInformationPacket(
                  id = packetId,
                  size = configuration.tcpHeaderSize
               ), path.reverse(), false, handler
            )
         }
      }
      // FIN
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path, false, handler
      )
      // ACK
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path.reverse(), false, handler
      )
      // FIN
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path.reverse(), false, handler
      )
      // ACK
      sendPackageToEndSuspended(
         SystemInformationPacket(
            id = idGenerator(),
            size = configuration.tcpHeaderSize
         ), path, false, handler
      )
      handler(Event.EndSimulationEvent(systemPackagesBytes, infoPackagesBytes), false)
   }

}