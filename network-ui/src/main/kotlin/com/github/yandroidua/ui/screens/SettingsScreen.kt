package com.github.yandroidua.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.components.EditText

// ------------------------------SettingsState--------------------------------------------------------------------------

data class SettingsState(
   val dumpPathState: MutableState<String>
)

// -------------------------------UI------------------------------------------------------------------------------------

@Composable
fun SettingsScreen(
   modifier: Modifier = Modifier,
   settingsState: SettingsState,
   dumper: (String) -> Unit,
   loader: @Composable (String) -> Unit
) = Column(modifier = modifier.padding(all = 8.dp).wrapContentSize()) {
   val loaderTrigger = remember { mutableStateOf(false) }
   Row {
      Text(text = "Dump file:", modifier = Modifier.align(alignment = Alignment.CenterVertically))
      Spacer(modifier = Modifier.width(width = 5.dp))
      EditText(
         value = settingsState.dumpPathState.value,
         onValueChange = { path -> settingsState.dumpPathState.value = path }
      )
   }
   Spacer(modifier = Modifier.height(8.dp))
   Button(
      modifier = Modifier.fillMaxWidth(),
      onClick = { dumper(settingsState.dumpPathState.value) }
   ) { Text("Dump") }
   Spacer(modifier = Modifier.height(8.dp))
   Button(
      modifier = Modifier.fillMaxWidth(),
      onClick = { loaderTrigger.value = true }
   ) { Text("Load") }
   if (loaderTrigger.value) {
      loader(settingsState.dumpPathState.value)
      loaderTrigger.value = false
   }
}