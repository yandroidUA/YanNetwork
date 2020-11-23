package com.github.yandroidua.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.utils.PathCalculationResult

//todo different UI for single workstation-workstation path and for workstation-any path ?

@Composable
fun ResultScreen(modifier: Modifier = Modifier, result: PathCalculationResult?) {
    if (result == null) {
        EmptyResultScreen(modifier)
        return
    }
    when (result) {
        is PathCalculationResult.SingleResult -> SingleResultScreen(modifier, result)
        is PathCalculationResult.MultiResult -> MultiResultScreen(modifier, result)
    }
}

@Composable
private fun SingleResultScreen(modifier: Modifier, result: PathCalculationResult.SingleResult) {
    Text(text = "This is SingleResultScreen")
}

@Composable
private fun MultiResultScreen(modifier: Modifier, result: PathCalculationResult.MultiResult) {
    Text(text = "This is MultiResultScreen")
}

@Composable
private fun EmptyResultScreen(modifier: Modifier) = Column(modifier.then(
        Modifier.fillMaxSize()
)) {
    Spacer(modifier = Modifier.weight(weight = 1/4f))
    Column(modifier = Modifier.weight(weight = 1/2f)
            .align(alignment = Alignment.CenterHorizontally)
            .background(Color.White)
    ) {
        Image(
                bitmap = imageFromResource("no_results.png"),
                modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(height = 10.dp).width(width = 1.dp))
        Text("No results",  modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
    }
    Spacer(modifier = Modifier.weight(weight = 1/4f))
}
