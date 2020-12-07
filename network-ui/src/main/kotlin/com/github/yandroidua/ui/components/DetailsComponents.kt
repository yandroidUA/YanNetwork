package com.github.yandroidua.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.SimulationRoutingTableEntry

@Composable
fun RouteTable(connections: List<SimulationRoutingTableEntry>) = Column(
   modifier = Modifier
      .fillMaxWidth()
      .border(width = 2.dp, color = Color.Black, shape = CutCornerShape(size = 4.dp))
      .padding(all = 5.dp)
) {
   Text(text = "Route table", modifier = Modifier.align(alignment = Alignment.CenterHorizontally))

   if (connections.isEmpty()) {
      Text(
         text = "Table is empty!", modifier = Modifier
            .align(alignment = Alignment.CenterHorizontally)
            .padding(all = 24.dp)
      )
   } else {
      for ((index, conn) in connections.withIndex()) {
         SingleRount(conn)
         if (index == connections.size - 1) {
            Spacer(modifier = Modifier.fillMaxWidth().height(height = 1.dp).background(Color.Black))
         }
      }
   }
}

@Composable
fun SingleRount(connection: SimulationRoutingTableEntry) = Column(
   modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
) {
   Spacer(modifier = Modifier.fillMaxWidth().height(height = 1.dp).background(Color.Black))
   Row(modifier = Modifier.fillMaxWidth()) {
      Row(modifier = Modifier.weight(1f)) {
         Row(modifier = Modifier.weight(0.5f)) {
            Spacer(modifier = Modifier.width(width = 1.dp).height(height = 20.dp).background(Color.Black))
            Spacer(modifier = Modifier.width(width = 4.dp))
            Text(text = "To: ${connection.toId}")
         }
         Row(modifier = Modifier.weight(0.5f)) {
            Spacer(modifier = Modifier.width(width = 1.dp).height(height = 20.dp).background(Color.Black))
            Spacer(modifier = Modifier.width(width = 8.dp))
            Text(text = "Send to: ${connection.fromId ?: "-"}")
         }
      }
      Spacer(modifier = Modifier.width(width = 1.dp).height(height = 20.dp).background(Color.Black))
   }
}
