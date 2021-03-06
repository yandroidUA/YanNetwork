package com.github.yandroidua.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.models.PathResultElements
import com.github.yandroidua.ui.models.SimulationResultModel

@Composable
fun SimulationScreen(
   modifier: Modifier = Modifier,
   path: PathResultElements,
   stopState: MutableState<Boolean>,
   startState: MutableState<Boolean>,
   simulationState: State<out SimulationResultModel?>,
   onRestart: () -> Unit,
   onStop: () -> Unit,
   onNext: () -> Unit,
   onResume: () -> Unit,
   onClose: () -> Unit
) {
   Column(
      modifier
         .background(Color.White)
         .fillMaxSize()
         .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
         .padding(10.dp)
   ) {
      Button(
         onClick = onClose,
         modifier = Modifier
            .padding(all = 4.dp)
            .wrapContentSize(align = Alignment.TopEnd)
            .align(Alignment.End)
      ) { Text(text = "Close") }
      Spacer(modifier = Modifier.height(height = 8.dp))
      Text(text = "${simulationState.value}", modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
      Spacer(modifier = Modifier.height(height = 10.dp))
      Path(path = path, steps = path.path.size, isSendEnabled = false)
      Spacer(modifier = Modifier.height(height = 10.dp))
      StatusButton(enabled = stopState.value, onClick = { onNext() }) { Text(text = "Next") }
      Spacer(modifier = Modifier.height(height = 10.dp))
      StatusButton(enabled = stopState.value.not(), onClick = { onStop() }) { Text(text = "Stop") }
      Spacer(modifier = Modifier.height(height = 10.dp))
      StatusButton(enabled = stopState.value, onClick = { onResume() }) { Text(text = "Resume") }
      Spacer(modifier = Modifier.height(height = 10.dp))
      StatusButton(onClick = { onRestart() }) { Text(text = if (startState.value) "Restart" else "Start") }
      Spacer(modifier = Modifier.height(height = 10.dp))
   }
}

@Composable
fun StatusButton(
   enabled: Boolean = true,
   onClick: () -> Unit,
   text: @Composable RowScope.() -> Unit
) {
   Surface(
      modifier = Modifier
         .fillMaxWidth()
         .alpha(alpha = if (enabled) 1f else 0.5f)
   ) {
      Button(onClick = onClick, content = text)
   }

}