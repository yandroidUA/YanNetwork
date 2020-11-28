package com.github.yandroidua.simulation

import com.github.yandroidua.simulation.models.*
import com.github.yandroidua.simulation.models.packets.InformationPacket
import com.github.yandroidua.simulation.models.packets.SystemInformationPacket
import kotlinx.coroutines.delay
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
        return if (configuration.mode == Mode.LOGICAL) { LOGIC_ADDITIONAL_PACKAGES_COUNT } else { 0 } +
                ceil((1f + connection.errorChance) * configuration.size / configuration.sysPacketSize).toInt()
    }

    /**
     * Make delay due to [SimulationConnection.type]
     */
    private suspend fun makeDelay(connection: SimulationConnection) {
        delay(1000)
    }

    private suspend fun FlowCollector<Event>.panic(text: String) {
        emit(Event.ErrorEvent(text))
    }

    private suspend fun FlowCollector<Event>.sendPackage(
            from: SimulationWorkstation,
            to: SimulationWorkstation,
            by: SimulationConnection
    ) {
        emit(Event.TextEvent("Sending information packages from ${from.id} to ${to.id}"))
        val informationPackagesCount = calculateInformationPackages(by)
        val systemInfPackaheCount = calculateSystemInformationPackages(by)
        makeDelay(by)
        emit(Event.SendPacketsEvent(packets = listOf(
                InformationPacket(informationPackagesCount),
                SystemInformationPacket(systemInfPackaheCount)
        )))
    }

    fun simulate(): Flow<Event> = flow {
//        emit(Event.TextEvent(text = "Simulation started"))
        val fromWorkstation: SimulationWorkstation? = models.find { it.id == (configuration.path?.from ?: it.id.minus(1)) } as? SimulationWorkstation

        if (fromWorkstation == null) {
            panic(text = "Cannot find start SimulationWorkstation with id == ${configuration.path?.from}")
            return@flow
        }

        var from: SimulationWorkstation = fromWorkstation
        for (pathEntry in configuration.path?.path ?: emptyList()) {
            println("A, size = ${configuration.path?.path?.size}")
            val connection = models.find { it.id == pathEntry.first } as? SimulationConnection

            if (connection == null) {
                panic(text = "Cannot find Connection with id = ${pathEntry.first}")
                return@flow
            }

            val workstation = models.find { it.id == pathEntry.second } as? SimulationWorkstation

            if (workstation == null) {
                panic(text = "Cannot find Workstation with id = ${pathEntry.second}")
                return@flow
            }

            sendPackage(from, workstation, connection)

            from = workstation
        }

        emit(Event.TextEvent(text = "Simulation ended"))
    }

}