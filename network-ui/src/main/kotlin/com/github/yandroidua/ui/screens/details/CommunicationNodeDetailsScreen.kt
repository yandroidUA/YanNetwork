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
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.utils.VovaTheming


@Composable
fun CommunicationNodeDetailsScreen(
   modifier: Modifier = Modifier,
   elementCommunicationNode: ElementCommunicationNode,
   connections: List<SimulationRoutingTableEntry>,
   saver: (ElementCommunicationNode) -> Unit,
   deleter: (ElementCommunicationNode) -> Unit
) = Column(modifier) {
   val networkState = remember { mutableStateOf(elementCommunicationNode.network) }
   Column(modifier = Modifier.weight(1f)) {
      Text(
         text = "Комунікаційний вузол №${elementCommunicationNode.id}",
         modifier = Modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .align(alignment = Alignment.CenterHorizontally)
      )
      Spacer(modifier = Modifier.height(height = 8.dp))
      Row {
         Text(text = "Мережа:", modifier = Modifier.align(alignment = Alignment.CenterVertically))
         Spacer(modifier = Modifier.width(width = 4.dp))
         EditText(
            value = networkState.value ?: "NaN",
            onValueChange = { networkState.value = it }
         )
      }
      Spacer(modifier = Modifier.height(height = 8.dp))
      RouteTable(connections)
   }
   Button(
      colors = VovaTheming.buttonColors(),
      onClick = { deleter(elementCommunicationNode) },
      modifier = Modifier.fillMaxWidth()
   ) {
      Text(text = "Видалити", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
   Spacer(modifier = Modifier.height(height = 4.dp))
   Button(
      colors = VovaTheming.buttonColors(),
      onClick = {
         elementCommunicationNode.network = networkState.value
         saver(elementCommunicationNode)
      }, modifier = Modifier.fillMaxWidth()
   ) {
      Text(text = "Зберегти", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
}