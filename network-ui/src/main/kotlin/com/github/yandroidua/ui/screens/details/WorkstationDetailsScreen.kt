package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.Workstation

@Composable
fun WorkstationDetails(modifier: Modifier = Modifier, workstation: Workstation) = Column(modifier) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
                text = "This is Workstation#${workstation.id}",
                modifier = Modifier
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .align(alignment = Alignment.CenterHorizontally)
        )
        Button(onClick = {}) { Text("Test1") }
        Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
        Button(onClick = {}) { Text("Test2") }
        Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
        Button(onClick = {}) { Text("Test3") }
        Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
        Button(onClick = {}) { Text("Test4") }
    }
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Save", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
    }
}