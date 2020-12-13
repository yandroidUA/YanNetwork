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
import com.github.yandroidua.ui.utils.DaeerTheming

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
         text = "Робоча станція №${elementWorkstation.id}",
         modifier = Modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .align(alignment = Alignment.CenterHorizontally)
      )
      Spacer(modifier = Modifier.height(height = 8.dp))
      Row {
         Text(text = "Мережа:", modifier = Modifier.align(alignment = Alignment.CenterVertically))
         Spacer(modifier = Modifier.width(width = 4.dp))
         EditText(
            value = workstationNetworkState.value ?: "NaN",
            onValueChange = { workstationNetworkState.value = it }
         )
      }
      Spacer(modifier = Modifier.height(height = 8.dp))
      RouteTable(connections)
   }
   Button(
      colors = DaeerTheming.buttonColors(),
      onClick = { deleter(elementWorkstation) },
      modifier = Modifier.fillMaxWidth()
   ) {
      Text(text = "Видалити", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
   Spacer(modifier = Modifier.height(height = 4.dp))
   Button(
      colors = DaeerTheming.buttonColors(),
      onClick = {
         elementWorkstation.network = workstationNetworkState.value
         saver(elementWorkstation)
      }, modifier = Modifier.fillMaxWidth()
   ) {
      Text(text = "Зберегти", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
}