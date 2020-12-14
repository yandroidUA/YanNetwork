package com.github.yandroidua.ui.screens.calculation

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yandroidua.algorithm.BellmanFordAlgorithm
import com.github.yandroidua.algorithm.models.Line
import com.github.yandroidua.algorithm.models.Workstation
import com.github.yandroidua.simulation.models.Mode
import com.github.yandroidua.ui.components.EditText
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.mappers.mapToAlgorithmEntity
import com.github.yandroidua.ui.models.PathCalculationResult
import com.github.yandroidua.ui.screens.drawer.SimulationContext
import com.github.yandroidua.ui.utils.DaeerTheming

@OptIn(ExperimentalKeyInput::class)
fun CalculationWindow(
   workstations: List<ConnectableElement>,
   lines: List<ElementLine>,
   simulationContext: SimulationContext,
   onCalculated: (PathCalculationResult) -> Unit
) {
   val window = AppWindow(size = IntSize(600, 1000), title = "Налаштування симуляції").also {
      it.keyboard.setShortcut(Key.Escape) {
         it.close()
      }
   }
   window.show {
      val state = remember {
         CalculationContext(
            fromWorkstationState = mutableStateOf(null),
            toWorkstationState = mutableStateOf(null),
            workstationFromDropDownState = mutableStateOf(false),
            workstationToDropDownState = mutableStateOf(false),
            messageSizeState = mutableStateOf(simulationContext.size.toString()),
            tcpHeaderState = mutableStateOf(simulationContext.tcpHeaderSize.toString()),
            infoPacketSizeState = mutableStateOf(simulationContext.infoPacketSize.toString()),
            simulationModeState = mutableStateOf(simulationContext.mode),
            udpHeaderSizeState = mutableStateOf(simulationContext.udpHeaderSize.toString()),
            byLengthState = mutableStateOf(simulationContext.byLength),
            calculationFromErrorContext = CalculationFromErrorContext(
               workstationFromErrorState = mutableStateOf(false),
               messageSizeErrorState = mutableStateOf(null),
               informationPackageSizeErrorState = mutableStateOf(null),
               tcpHeaderError = mutableStateOf(null),
               udpHeaderSizeState = mutableStateOf(null)
            )
         )
      }

      Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
         Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            Row {
               Text(
                  text = "Станція-відпраник:",
                  color = if (state.calculationFromErrorContext.workstationFromErrorState.value) Color.Red else Color.Black
               )
               Spacer(modifier = Modifier.height(1.dp).width(5.dp))
               DropdownMenu(
                  toggleModifier = Modifier.wrapContentSize(),
                  dropdownModifier = Modifier.wrapContentSize(),
                  toggle = {
                     Text(state.fromWorkstationState.value?.id?.toString() ?: "Оберіть станцію", modifier = Modifier
                        .clickable { state.workstationFromDropDownState.value = true })
                  },
                  expanded = state.workstationFromDropDownState.value,
                  onDismissRequest = { state.workstationFromDropDownState.value = false },
               ) {
                  for (workstation in workstations.filterIsInstance<ElementWorkstation>()) {
                     DropdownMenuItem(
                        onClick = {
                           state.calculationFromErrorContext.workstationFromErrorState.value = false
                           state.workstationFromDropDownState.value = false
                           state.fromWorkstationState.value = workstation
                        }
                     ) { Text(text = workstation.id.toString()) }
                  }
               }
            }
            Row {
               Text(text = "Станція отримувач:")
               Spacer(modifier = Modifier.height(1.dp).width(5.dp))
               DropdownMenu(
                  toggleModifier = Modifier.wrapContentSize(),
                  dropdownModifier = Modifier.wrapContentSize(),
                  toggle = {
                     Text(state.toWorkstationState.value?.id?.toString() ?: "Усі", modifier = Modifier
                        .clickable { state.workstationToDropDownState.value = true })
                  },
                  expanded = state.workstationToDropDownState.value,
                  onDismissRequest = { state.workstationToDropDownState.value = false }
               ) {
                  for (workstation in workstations.filterIsInstance<ElementWorkstation>()) {
                     DropdownMenuItem(
                        onClick = {
                           state.workstationToDropDownState.value = false
                           state.toWorkstationState.value = workstation
                        }
                     ) { Text(text = workstation.id.toString()) }
                  }
               }
            }
            Spacer(modifier = Modifier.height(height = 8.dp))
            Row {
               Text(text = "Розмір повідомлення: ", modifier = Modifier.align(alignment = Alignment.CenterVertically))
               Spacer(modifier = Modifier.width(width = 5.dp))
               EditText(
                  error = state.calculationFromErrorContext.messageSizeErrorState.value,
                  value = state.messageSizeState.value,
                  onValueChange = { text ->
                     validateInputToInt(
                        text,
                        valueState = state.messageSizeState,
                        errorState = state.calculationFromErrorContext.messageSizeErrorState
                     )
                  }
               )
            }
            Spacer(modifier = Modifier.height(height = 8.dp))
            Row {
               Text(
                  text = "MTU : ",
                  modifier = Modifier.align(alignment = Alignment.CenterVertically)
               )
               Spacer(modifier = Modifier.width(width = 5.dp))
               EditText(
                  error = state.calculationFromErrorContext.informationPackageSizeErrorState.value,
                  value = state.infoPacketSizeState.value,
                  onValueChange = { text ->
                     validateInputToInt(
                        text,
                        valueState = state.infoPacketSizeState,
                        errorState = state.calculationFromErrorContext.informationPackageSizeErrorState
                     )
                  }
               )
            }
            Spacer(modifier = Modifier.height(height = 8.dp))
            Column(
               modifier = Modifier
                  .wrapContentSize()
                  .align(alignment = Alignment.CenterHorizontally)
                  .border(
                     shape = RoundedCornerShape(size = 4.dp),
                     color = Color.Black,
                     width = 2.dp
                  ).padding(all = 8.dp)
            ) {
               ModeRadioButton(state.simulationModeState, Mode.LOGICAL)
               Spacer(modifier = Modifier.height(height = 4.dp))
               ModeRadioButton(state.simulationModeState, Mode.VIRTUAL)
               Spacer(modifier = Modifier.height(height = 4.dp))
               ModeRadioButton(state.simulationModeState, Mode.DATAGRAM)
            }
            Spacer(modifier = Modifier.height(height = 8.dp))
            Row {
               Text("Найменша кількість станцій", modifier = Modifier.align(alignment = Alignment.CenterVertically))
               Spacer(modifier = Modifier.width(width = 4.dp))
               Switch(
                  checked = state.byLengthState.value,
                  onCheckedChange = { state.byLengthState.value = it },
                  modifier = Modifier.align(alignment = Alignment.CenterVertically)
               )
               Spacer(modifier = Modifier.width(width = 4.dp))
               Text("Найменша вага", modifier = Modifier.align(alignment = Alignment.CenterVertically))
            }
         }
         Button(
            colors = DaeerTheming.buttonColors(),
            onClick = {
               if (state.fromWorkstationState.value == null) {
                  state.calculationFromErrorContext.workstationFromErrorState.value = true
               } else if (
                  state.calculationFromErrorContext.informationPackageSizeErrorState.value == null
                  && state.calculationFromErrorContext.messageSizeErrorState.value == null
                  && state.calculationFromErrorContext.tcpHeaderError.value == null
                  && state.calculationFromErrorContext.udpHeaderSizeState.value == null
               ) {
                  state.calculationFromErrorContext.workstationFromErrorState.value = false
                  simulationContext.mode = state.simulationModeState.value
                  simulationContext.size = state.messageSizeState.value.toInt()
                  simulationContext.infoPacketSize = state.infoPacketSizeState.value.toInt()
                  simulationContext.tcpHeaderSize = state.tcpHeaderState.value.toInt()
                  simulationContext.fromId = state.fromWorkstationState.value?.id ?: 0
                  simulationContext.toId = state.toWorkstationState.value?.id ?: 0
                  simulationContext.udpHeaderSize = state.udpHeaderSizeState.value.toInt()
                  simulationContext.byLength = state.byLengthState.value
                  onCalculated(
                     clicked(
                        workstations = workstations.map { it.mapToAlgorithmEntity() },
                        lines = lines.map { it.mapToAlgorithmEntity() },
                        from = state.fromWorkstationState.value!!.mapToAlgorithmEntity(),
                        to = state.toWorkstationState.value?.mapToAlgorithmEntity(),
                        byLength = state.byLengthState.value
                     )
                  )
                  window.close()
               }

            },
            modifier = Modifier.fillMaxWidth()
         ) { Text(text = "Старт") }
      }
   }
}

@Composable
private fun ModeRadioButton(simulationModeState: MutableState<Mode>, mode: Mode) {
   Row {
      RadioButton(selected = simulationModeState.value == mode, onClick = { simulationModeState.value = mode })
      Spacer(modifier = Modifier.width(width = 4.dp))
      Text(text = mode.uiName, modifier = Modifier.align(alignment = Alignment.CenterVertically))
   }
}

//----------------------------------------------------------------------------------------------------------------------

private fun validateInputToInt(
   text: String,
   errorState: MutableState<String?>,
   valueState: MutableState<String>
) {
   val value = text.toIntOrNull()
   if (value == null) {
      errorState.value = "Not valid number!"
      valueState.value = text
      return
   }
   errorState.value = null
   valueState.value = value.toString()
}

private fun clicked(
   workstations: List<Workstation>,
   lines: List<Line>,
   from: Workstation,
   byLength: Boolean,
   to: Workstation?
): PathCalculationResult {
   val bellmanFordAlgorithm = BellmanFordAlgorithm(workstations, lines)
   if (to == null) return PathCalculationResult(
      paths = bellmanFordAlgorithm.calculate(
         from = from,
         byLength = byLength
      )
   )
   return PathCalculationResult(paths = bellmanFordAlgorithm.calculate(from = from, to = to, byLength = byLength))
}
