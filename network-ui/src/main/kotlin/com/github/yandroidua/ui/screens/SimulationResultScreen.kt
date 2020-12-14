package com.github.yandroidua.ui.screens

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.Mode
import java.text.SimpleDateFormat
import java.util.*

fun SimulationResultWindow(
   messageSize: Int,
   infoPacketsCount: Int,
   packageInformationSize: Int,
   systemPacketsCount: Int,
   informationTraffic: Int,
   systemTraffic: Int,
   errorCount: Int,
   mode: Mode,
   startTime: Long,
   endTime: Long
) = AppWindow(size = IntSize(800, 600), title = "Результати").show {
   val simpleDateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss.SSS")
   Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.align(Alignment.Center)) {
         Text(text = "Розмір повідомлення: $messageSize")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "К-ть інформаційних пакетів: $infoPacketsCount")
         Spacer(modifier = Modifier.height(height = 4.dp))
//         Text(text = "К-ть системних пакетів: ${if (mode == Mode.DATAGRAM) infoPacketsCount else infoPacketsCount + 7}")
         Text(text = "К-ть системних пакетів: $systemPacketsCount")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "К-ть втрачених пакетів: $errorCount")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "MTU: $packageInformationSize")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "Інформаційний трафік: $informationTraffic")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "Системний трафік: $systemTraffic")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "Час початку симуляції: $startTime (${simpleDateFormat.format(Date(startTime))})")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "Час закінчення симуляції: $endTime (${simpleDateFormat.format(Date(endTime))})")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "Тривалість: ${(endTime - startTime) / 10.0} у.о.")
         Spacer(modifier = Modifier.height(height = 4.dp))
         Text(text = "Режим: ${mode.uiName}")
         Spacer(modifier = Modifier.height(height = 4.dp))
      }
   }
}
/*

      inf: header + infa = MTU -> white
      inf.trafin: infa
      sys.trafic: header


      sys: header + sys.infa (50) = sys.pack -> blue


 */