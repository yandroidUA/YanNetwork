package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.yandroidua.ui.components.RouteTable
import com.github.yandroidua.ui.elements.ElementWorkstation

@Composable
fun WorkstationDetails(
   modifier: Modifier = Modifier,
   elementWorkstation: ElementWorkstation,
   connections: List<Pair<Int, Pair<String, Int>>>,
   deleter: (ElementWorkstation) -> Unit,
) = Column(modifier) {
   Column(modifier = Modifier.weight(1f)) {
      Text(
         text = "This is Workstation#${elementWorkstation.id}",
         modifier = Modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .align(alignment = Alignment.CenterHorizontally)
      )
      RouteTable(connections)
   }
   Button(onClick = { deleter(elementWorkstation) }, modifier = Modifier.fillMaxWidth()) {
      Text(text = "Delete", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }
}