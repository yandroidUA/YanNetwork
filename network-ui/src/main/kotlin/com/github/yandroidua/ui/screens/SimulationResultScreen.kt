package com.github.yandroidua.ui.screens

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.Mode
import java.text.SimpleDateFormat
import java.util.*

fun SimulationResultWindow(
   messageSize: Int,
   frameSystemHeaderSize: Int,
   packageInformationSize: Int,
   packageSystemSize: Int,
   informationTraffic: Int,
   systemTraffic: Int,
   mode: Mode,
   startTime: Long,
   endTime: Long
) = AppWindow(size = IntSize(800, 600), title = "Results").show {
   val simpleDateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss.SSS")
   Column(modifier = Modifier.fillMaxWidth()) {
      Text(text = "Message size: $messageSize")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Frame header size: $frameSystemHeaderSize")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Info. package size: $packageInformationSize")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Sys. package size: $packageSystemSize")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Info. traffic: $informationTraffic")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Sys. traffic: $systemTraffic")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Start time: $startTime (${simpleDateFormat.format(Date(startTime))})")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "End time: $endTime (${simpleDateFormat.format(Date(startTime))})")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Duration: ${endTime - startTime} millis")
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "Mode: ${mode.name}")
      Spacer(modifier = Modifier.height(height = 4.dp))
   }
}
