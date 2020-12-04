package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.*
import com.github.yandroidua.simulation.models.packets.InformationPacket
import com.github.yandroidua.simulation.models.packets.SystemInformationPacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.math.ceil

class Simulation(
    private val configuration: Configuration,
    private val models: List<SimulationModel>
) {

   companion object {
      private const val LOGIC_ADDITIONAL_PACKAGES_COUNT = 2 //for connection
      const val INFO_EMITS_PER_SEND = 1 // emits Before Event.SendPacketsEvent
   }

   private fun calculateInformationPackages(connection: SimulationConnection): Int {
      return ceil((1f + connection.errorChance) * configuration.size / configuration.sysPacketSize).toInt()
   }

   private fun calculateSystemInformationPackages(connection: SimulationConnection): Int {
      return if (configuration.mode == Mode.LOGICAL) {
         LOGIC_ADDITIONAL_PACKAGES_COUNT
      } else {
         0
      } +
          ceil((1f + connection.errorChance) * configuration.size / configuration.sysPacketSize).toInt()
   }

   private fun calculateDelay(connection: SimulationConnection): Long {
      return 200
   }

   private fun panic(events: MutableList<Event>, text: String) {
      events.add(Event.ErrorEvent(text))
   }

   private fun sendPackage(
       events: MutableList<Event>,
       from: SimulationWorkstation,
       to: SimulationWorkstation,
       by: SimulationConnection
   ) {
      events.add(Event.TextEvent("Sending information packages from ${from.id} to ${to.id}"))
      val informationPackagesCount = calculateInformationPackages(by)
      val systemInfPackageCount = calculateSystemInformationPackages(by)
       events.add(Event.SendPacketsEvent(
          packets = listOf(
              InformationPacket(informationPackagesCount),
              SystemInformationPacket(systemInfPackageCount)
          ),
          lineId = by.id,
          fromStationId = from.id,
          toStationId = to.id,
          time = calculateDelay(by)
      ))
   }

   fun simulate(): List<Event> {
       val events = mutableListOf<Event>()
//        emit(Event.TextEvent(text = "Simulation started"))
      val fromWorkstation: SimulationWorkstation? = models.find {
         it.id == (configuration.path?.from ?: it.id.minus(1))
      } as? SimulationWorkstation

      if (fromWorkstation == null) {
         panic(events, text = "Cannot find start SimulationWorkstation with id == ${configuration.path?.from}")
         return events
      }

      var from: SimulationWorkstation = fromWorkstation
      for ((index, pathEntry) in (configuration.path?.path ?: emptyList()).withIndex()) {
         if (index == 0) continue
         val connection = models.find { it.id == pathEntry.first } as? SimulationConnection

         if (connection == null) {
            panic(events, text = "Cannot find Connection with id = ${pathEntry.first}")
            return events
         }

         val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation

         if (workstation == null) {
            panic(events, text = "Cannot find Workstation with id = ${pathEntry.second}")
            return events
         }

         sendPackage(events, from, workstation, connection)

         from = workstation
      }

       events.add(Event.EndSimulationEvent)
       return events
   }

}