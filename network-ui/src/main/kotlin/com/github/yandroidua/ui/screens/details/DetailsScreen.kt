package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation

@Composable
fun DetailsScreen(modifier: Modifier = Modifier, element: Element, saver: (Element) -> Unit) = Column(modifier.then(
        Modifier
                .background(Color.White)
                .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
                .padding(10.dp)
)) {
    when (element.type) {
        ElementType.WORKSTATION -> WorkstationDetails(elementWorkstation = element as ElementWorkstation)
        ElementType.LINE -> LineDetails(elementLine = element as ElementLine) { saver(it) }
        ElementType.COMMUNICATION_NODE -> CommunicationNodeDetailsScreen(elementCommunicationNode = element as ElementCommunicationNode)
    }
}


