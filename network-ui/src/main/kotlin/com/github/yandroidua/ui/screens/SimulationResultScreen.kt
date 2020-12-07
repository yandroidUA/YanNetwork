package com.github.yandroidua.ui.screens

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.github.yandroidua.simulation.models.Mode

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
) = AppWindow(size = IntSize(800, 600)).show {
   Row(modifier = Modifier.fillMaxWidth()) {

   }
}
