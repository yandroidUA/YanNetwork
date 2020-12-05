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

//TODO: запускать алогритм для поиска оптимального пути в совей локальной мережы

@Composable
fun RouteTable(connections: List<Pair<Int, Pair<String, Int>>>) = Column(modifier = Modifier
   .fillMaxWidth()
   .padding(all = 5.dp)
   .border(width = 2.dp, color = Color.Black, shape = CutCornerShape(size = 4.dp))
   .padding(all = 5.dp)
) {
   Text(text = "Route table", modifier = Modifier.align(alignment = Alignment.CenterHorizontally))

   if (connections.isEmpty()) {
      Text(text = "Table is empty!", modifier = Modifier
         .align(alignment = Alignment.CenterHorizontally)
         .padding(all = 24.dp)
      )
   } else {
      for (conn in connections) {
         SingleRount(conn)
      }
   }
}

@Composable
fun SingleRount(connection: Pair<Int, Pair<String, Int>>) = Column (
   modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
) {
   Spacer(modifier = Modifier.fillMaxWidth().height(height = 1.dp).background(Color.Black))
   Row(modifier = Modifier.fillMaxWidth()) {
      Row(modifier = Modifier.weight(1f)) {
         Spacer(modifier = Modifier.width(width = 1.dp).height(height = 20.dp).background(Color.Black))
         Spacer(modifier = Modifier.width(width = 4.dp))
         Text(text = "LineId: ${connection.first}")
         Spacer(modifier = Modifier.width(width = 1.dp).height(height = 20.dp).background(Color.Black))
         Spacer(modifier = Modifier.width(width = 8.dp))
         Text(text = "${connection.second.first}: ${connection.second.second}")
      }
      Spacer(modifier = Modifier.width(width = 1.dp).height(height = 20.dp).background(Color.Black))
   }
   Spacer(modifier = Modifier.fillMaxWidth().height(height = 1.dp).background(Color.Black))
}