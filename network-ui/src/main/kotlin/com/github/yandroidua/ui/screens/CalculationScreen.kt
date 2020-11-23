package com.github.yandroidua.ui.screens

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yandroidua.algorithm.BellmanFordAlgorithm
import com.github.yandroidua.algorithm.Line
import com.github.yandroidua.algorithm.Workstation
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.mappers.mapToAlgorithmEntity
import com.github.yandroidua.ui.utils.PathCalculationResult

//--------------------------------CalculationState----------------------------------------------------------------------

data class CalculationState(
        val fromWorkstation: ConnectableElement? = null,
        val toWorkstation: ConnectableElement? = null
)

//--------------------------------Utils functions-----------------------------------------------------------------------

private fun onCalculateClicked(
        workstations: List<Workstation>,
        lines: List<Line>,
        from: Workstation,
        to: Workstation?
): PathCalculationResult {
    val bellmanFordAlgorithm = BellmanFordAlgorithm(workstations, lines)
    if (to == null) {
        return PathCalculationResult(
                paths = bellmanFordAlgorithm.calculate(from = from)
        )
    }
    return PathCalculationResult(
            paths = bellmanFordAlgorithm.calculate(from = from, to = to)
    )
}


//--------------------------------UI------------------------------------------------------------------------------------

@OptIn(ExperimentalKeyInput::class)
fun CalculationWindow(
        workstations: List<ConnectableElement>,
        lines: List<ElementLine>,
        onCalculated: (PathCalculationResult) -> Unit
) {
    val window = AppWindow(size = IntSize(400, 300)).also {
        it.keyboard.setShortcut(Key.Escape) {
            it.close()
        }
    }
    window.show {
        val workstationToDropDownState = remember { mutableStateOf(false) }
        val workstationFromDropDownState = remember { mutableStateOf(false) }
        val calcState = remember { mutableStateOf(CalculationState()) }
        val errorFromState = remember { mutableStateOf(false) }
        Column(modifier = Modifier.padding(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(text = "From workstation:", color = if (errorFromState.value) Color.Red else Color.Black)
                    Spacer(modifier = Modifier.height(1.dp).width(5.dp))
                    DropdownMenu(
                            toggleModifier = Modifier.wrapContentSize(),
                            dropdownModifier = Modifier.wrapContentSize(),
                            toggle = {
                                Text(calcState.value.fromWorkstation?.id?.toString() ?: "Select station", modifier = Modifier
                                        .clickable { workstationFromDropDownState.value = true })
                            },
                            expanded = workstationFromDropDownState.value,
                            onDismissRequest = { workstationFromDropDownState.value = false }
                    ) {
                        //todo this must be fixed in further build of compose
                        for (workstation in workstations) {
                            DropdownMenuItem(
                                    onClick = {
                                        errorFromState.value = false
                                        workstationFromDropDownState.value = false
                                        calcState.value = calcState.value.copy(fromWorkstation = workstation)
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
                                Text(calcState.value.toWorkstation?.id?.toString() ?: "All", modifier = Modifier
                                        .clickable { workstationToDropDownState.value = true })
                            },
                            expanded = workstationToDropDownState.value,
                            onDismissRequest = { workstationToDropDownState.value = false }
                    ) {
                        //todo this must be fixed in further build of compose
                        for (workstation in workstations) {
                            DropdownMenuItem(
                                    onClick = {
                                        workstationToDropDownState.value = false
                                        calcState.value = calcState.value.copy(toWorkstation = workstation)
                                    }
                            ) { Text(text = workstation.id.toString()) }
                        }
                    }
                }
            }
            Button(onClick = {
                if (calcState.value.fromWorkstation == null) {
                    errorFromState.value = true
                } else {
                    errorFromState.value = false
                    onCalculated(onCalculateClicked(
                            workstations = workstations.map { it.mapToAlgorithmEntity() },
                            lines = lines.map { it.mapToAlgorithmEntity() },
                            from = calcState.value.fromWorkstation!!.mapToAlgorithmEntity(),
                            to = calcState.value.toWorkstation?.mapToAlgorithmEntity()
                    ))
                    window.close()
                }

            }, modifier = Modifier.fillMaxWidth()) { Text(text = "Calculate") }
        }
    }
}
