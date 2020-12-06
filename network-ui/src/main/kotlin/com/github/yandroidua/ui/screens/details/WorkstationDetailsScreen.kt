package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.SimulationRoutingTableEntry
import com.github.yandroidua.ui.components.EditText
import com.github.yandroidua.ui.components.RouteTable
import com.github.yandroidua.ui.elements.ElementWorkstation

@Composable
fun WorkstationDetails(
   modifier: Modifier = Modifier,
   elementWorkstation: ElementWorkstation,
   connections: List<SimulationRoutingTableEntry>,
   saver: (ElementWorkstation) -> Unit,
   deleter: (ElementWorkstation) -> Unit,
) = Column(modifier) {
   val workstationNetworkState = remember { mutableStateOf(elementWorkstation.network) }
   Column(modifier = Modifier.weight(1f)) {
      Text(
         text = "This is Workstation#${elementWorkstation.id}",
         modifier = Modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .align(alignment = Alignment.CenterHorizontally)
      )
      Spacer(modifier = Modifier.height(height = 8.dp))
      Row {
         Text(text = "Network:", modifier = Modifier.align(alignment = Alignment.CenterVertically))
         Spacer(modifier = Modifier.width(width = 4.dp))
         EditText(
            value = workstationNetworkState.value ?: "NaN",
            onValueChange = { workstationNetworkState.value = it }
         )
      }
      Spacer(modifier = Modifier.height(height = 8.dp))
      RouteTable(connections)
   }
   Button(onClick = { deleter(elementWorkstation) }, modifier = Modifier.fillMaxWidth()) {
      Text(text = "Delete", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
   Spacer(modifier = Modifier.height(height = 4.dp))
   Button(onClick = {
      elementWorkstation.network = workstationNetworkState.value
      saver(elementWorkstation)
   }, modifier = Modifier.fillMaxWidth()) {
      Text(text = "Save", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
}