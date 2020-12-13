package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.SimulationRoutingTableEntry
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.utils.VovaTheming

@Composable
fun DetailsScreen(
   modifier: Modifier = Modifier,
   element: Element,
   connections: List<SimulationRoutingTableEntry>,
   deleter: (Element) -> Unit,
   saver: (Element) -> Unit,
   closer: () -> Unit
) = Column(
   modifier.then(
      Modifier
         .background(Color.White)
         .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
         .padding(10.dp)
   )
) {
   Button(
      colors = VovaTheming.buttonColors(),
      onClick = closer,
      modifier = Modifier.align(Alignment.End)
   ) { Text(text = "X", color = Color.Red) }
   when (element.type) {
      ElementType.WORKSTATION -> WorkstationDetails(
         elementWorkstation = element as ElementWorkstation,
         deleter = deleter,
         connections = connections,
         saver = saver
      )
      ElementType.LINE -> LineDetails(elementLine = element as ElementLine, deleter = deleter, saver = saver)
      ElementType.COMMUNICATION_NODE -> CommunicationNodeDetailsScreen(
         elementCommunicationNode = element as ElementCommunicationNode,
         deleter = deleter,
         connections = connections,
         saver = saver
      )
      else -> {}
   }
}


