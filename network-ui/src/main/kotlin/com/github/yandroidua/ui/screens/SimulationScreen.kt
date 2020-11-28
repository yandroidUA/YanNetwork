package com.github.yandroidua.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.Event
import com.github.yandroidua.ui.utils.PathResultElements

@Composable
fun SimulationScreen(
        modifier: Modifier = Modifier,
        path: PathResultElements,
        simulationState: State<Event>,
        onRestart: () -> Unit,
        onStep: (Int) -> Unit
) {
    Column(modifier
            .background(Color.White)
            .fillMaxSize()
            .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
            .padding(10.dp)
    ) {
        Text(text = "${simulationState.value}", modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(height = 10.dp))
        Path(path = path, steps = path.path.size, isSendEnabled = false)
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(onClick = { onStep(2) }) { Text(text = "Start 2") }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(onClick = { onRestart() }) { Text(text = "Restart") }
        Spacer(modifier = Modifier.height(height = 10.dp))
    }
}