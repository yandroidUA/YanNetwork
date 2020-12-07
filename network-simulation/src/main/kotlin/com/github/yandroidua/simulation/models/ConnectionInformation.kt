package com.github.yandroidua.simulation.models

import kotlinx.coroutines.sync.Mutex

class ConnectionInformation(
   lines: List<SimulationConnection>
) {

   private val connMutex = Mutex(locked = false)
   private val maxWeight = lines.maxByOrNull { it.weight }?.weight?.plus(1) ?: 0
   private val connectionInfoList = lines.map { ConnectionPower(it.id, maxWeight - it.weight) }

   suspend fun trySend(connectionId: ConnectionId): Boolean {
      connMutex.lock()
      val answer = connectionInfoList.find { it.connectionId == connectionId }?.let { conn ->
         (conn.availablePlaces > 0).also { if (it) conn.availablePlaces-- }
      } ?: false
      connMutex.unlock()
      return answer
   }

   suspend fun release(connectionId: ConnectionId) {
      connMutex.lock()
      connectionInfoList.find { it.connectionId == connectionId }?.let { conn -> conn.availablePlaces++ }
      connMutex.unlock()
   }

}
