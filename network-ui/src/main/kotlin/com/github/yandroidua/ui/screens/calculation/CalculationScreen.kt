package com.github.yandroidua.ui.screens.calculation

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalKeyInput::class)
fun CalculationWindow(
   workstations: List<ConnectableElement>,
   lines: List<ElementLine>,
   simulationContext: SimulationContext,
   onCalculated: (PathCalculationResult) -> Unit
) {
   val window = AppWindow(size = IntSize(600, 1000), title = "Configuration").also {
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
            systemPacketSizeState = mutableStateOf(simulationContext.sysPacketSize.toString()),
            infoPacketSizeState = mutableStateOf(simulationContext.infoPacketSize.toString()),
            simulationModeState = mutableStateOf(simulationContext.mode),
            frameSysSizeState = mutableStateOf(simulationContext.frameSysSize.toString()),
            calculationFromErrorContext = CalculationFromErrorContext(
               workstationFromErrorState = mutableStateOf(false),
               messageSizeErrorState = mutableStateOf(null),
               informationPackageSizeErrorState = mutableStateOf(null),
               systemPackageSizeErrorState = mutableStateOf(null),
               frameSysSizeState = mutableStateOf(null)
            )
         )
      }

      Column(modifier = Modifier.padding(10.dp)) {
         Column(modifier = Modifier.weight(1f)) {
            Row {
               Text(
                  text = "From workstation:",
                  color = if (state.calculationFromErrorContext.workstationFromErrorState.value) Color.Red else Color.Black
               )
               Spacer(modifier = Modifier.height(1.dp).width(5.dp))
               DropdownMenu(
                  toggleModifier = Modifier.wrapContentSize(),
                  dropdownModifier = Modifier.wrapContentSize(),
                  toggle = {
                     Text(state.fromWorkstationState.value?.id?.toString() ?: "Select station", modifier = Modifier
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
               Text(text = "To workstation:")
               Spacer(modifier = Modifier.height(1.dp).width(5.dp))
               DropdownMenu(
                  toggleModifier = Modifier.wrapContentSize(),
                  dropdownModifier = Modifier.wrapContentSize(),
                  toggle = {
                     Text(state.toWorkstationState.value?.id?.toString() ?: "All", modifier = Modifier
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
               Text(text = "Message size: ", modifier = Modifier.align(alignment = Alignment.CenterVertically))
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
                  text = "Information package size: ",
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
            Row {
               Text(text = "System package size: ", modifier = Modifier.align(alignment = Alignment.CenterVertically))
               Spacer(modifier = Modifier.width(width = 5.dp))
               EditText(
                  error = state.calculationFromErrorContext.systemPackageSizeErrorState.value,
                  value = state.systemPacketSizeState.value,
                  onValueChange = { text ->
                     validateInputToInt(
                        text,
                        valueState = state.systemPacketSizeState,
                        errorState = state.calculationFromErrorContext.systemPackageSizeErrorState
                     )
                  }
               )
            }
            Spacer(modifier = Modifier.height(height = 8.dp))
            Row {
               Text(text = "Frame system. size: ", modifier = Modifier.align(alignment = Alignment.CenterVertically))
               Spacer(modifier = Modifier.width(width = 5.dp))
               EditText(
                  error = state.calculationFromErrorContext.frameSysSizeState.value,
                  value = state.frameSysSizeState.value,
                  onValueChange = { text ->
                     validateInputToInt(
                        text,
                        valueState = state.frameSysSizeState,
                        errorState = state.calculationFromErrorContext.frameSysSizeState
                     )
                  }
               )
            }
            Spacer(modifier = Modifier.height(height = 8.dp))
            ModeRadioButton(state.simulationModeState, Mode.LOGICAL)
            Spacer(modifier = Modifier.height(height = 4.dp))
            ModeRadioButton(state.simulationModeState, Mode.DATAGRAM)
         }
         Button(
            onClick = {
               if (state.fromWorkstationState.value == null) {
                  state.calculationFromErrorContext.workstationFromErrorState.value = true
               } else if (
                  state.calculationFromErrorContext.informationPackageSizeErrorState.value == null
                  && state.calculationFromErrorContext.messageSizeErrorState.value == null
                  && state.calculationFromErrorContext.systemPackageSizeErrorState.value == null
                  && state.calculationFromErrorContext.frameSysSizeState.value == null
               ) {
                  state.calculationFromErrorContext.workstationFromErrorState.value = false
                  simulationContext.mode = state.simulationModeState.value
                  simulationContext.size = state.messageSizeState.value.toInt()
                  simulationContext.infoPacketSize = state.infoPacketSizeState.value.toInt()
                  simulationContext.sysPacketSize = state.systemPacketSizeState.value.toInt()
                  simulationContext.fromId = state.fromWorkstationState.value?.id ?: 0
                  simulationContext.toId = state.toWorkstationState.value?.id ?: 0
                  simulationContext.frameSysSize = state.frameSysSizeState.value.toInt()
                  onCalculated(
                     clicked(
                        workstations = workstations.map { it.mapToAlgorithmEntity() },
                        lines = lines.map { it.mapToAlgorithmEntity() },
                        from = state.fromWorkstationState.value!!.mapToAlgorithmEntity(),
                        to = state.toWorkstationState.value?.mapToAlgorithmEntity()
                     )
                  )
                  window.close()
               }

            },
            modifier = Modifier.fillMaxWidth()
         ) { Text(text = "Calculate") }
      }
   }
}

@Composable
private fun ModeRadioButton(simulationModeState: MutableState<Mode>, mode: Mode) {
   Row {
      RadioButton(selected = simulationModeState.value == mode, onClick = { simulationModeState.value = mode })
      Spacer(modifier = Modifier.width(width = 4.dp))
      Text(text = mode.name, modifier = Modifier.align(alignment = Alignment.CenterVertically))
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
   to: Workstation?
): PathCalculationResult {
   val bellmanFordAlgorithm = BellmanFordAlgorithm(workstations, lines)
   if (to == null) return PathCalculationResult(paths = bellmanFordAlgorithm.calculate(from = from))
   return PathCalculationResult(paths = bellmanFordAlgorithm.calculate(from = from, to = to))
}
