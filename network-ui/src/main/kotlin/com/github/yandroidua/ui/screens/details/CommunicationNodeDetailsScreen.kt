package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.components.RouteTable
import com.github.yandroidua.ui.elements.ElementCommunicationNode


@Composable
fun CommunicationNodeDetailsScreen(
        modifier: Modifier = Modifier,
        elementCommunicationNode: ElementCommunicationNode,
        connections: List<Pair<Int, Pair<String, Int>>>,
        deleter: (ElementCommunicationNode) -> Unit
) = Column(modifier) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
                text = "This is ElementCommunicationNode#${elementCommunicationNode.id}",
                modifier = Modifier
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .align(alignment = Alignment.CenterHorizontally)
        )
       RouteTable(connections)
    }
    Button(onClick = { deleter(elementCommunicationNode) }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Delete", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
    }
}