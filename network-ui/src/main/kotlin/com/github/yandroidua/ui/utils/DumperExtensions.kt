package com.github.yandroidua.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.github.yandroidua.dump.Dumper
import com.github.yandroidua.dump.models.ConfigDump
import com.github.yandroidua.dump.models.ElementsDump
import com.github.yandroidua.ui.AppState
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.mappers.mapToDump
import com.github.yandroidua.ui.mappers.mapToUiElement
import com.github.yandroidua.ui.screens.drawer.DrawerContext
import com.github.yandroidua.ui.screens.drawer.SimulationContext

fun Dumper.addToDump(workstation: ElementWorkstation): Dumper {
   return addToDump(workstation.mapToDump())
}

fun Dumper.addToDump(line: ElementLine): Dumper {
   return addToDump(line.mapToDump())
}

fun Dumper.addToDump(communicationNode: ElementCommunicationNode): Dumper {
   return addToDump(communicationNode.mapToDump())
}

fun Dumper.addToDumpWorkstations(workstations: List<ElementWorkstation>): Dumper {
   workstations.forEach { addToDump(it.mapToDump()) }
   return this
}

fun Dumper.addToDumpLines(lines: List<ElementLine>): Dumper {
   lines.forEach { addToDump(it.mapToDump()) }
   return this
}

fun Dumper.addToDumpCommunicationNodes(communicationNodes: List<ElementCommunicationNode>): Dumper {
   communicationNodes.forEach { addToDump(it.mapToDump()) }
   return this
}

fun Dumper.addToDumpElements(elements: List<Element>): Dumper {
   val workstations = elements.filterIsInstance<ElementWorkstation>()
   val lines = elements.filterIsInstance<ElementLine>()
   val communicationNodes = elements.filterIsInstance<ElementCommunicationNode>()
   addToDumpCommunicationNodes(communicationNodes)
   addToDumpLines(lines)
   return addToDumpWorkstations(workstations)
}

fun ElementsDump.mapToElements(): List<Element> {
   val elements: MutableList<Element> = mutableListOf()
   elements.addAll(workstations.map { it.mapToUiElement() })
   elements.addAll(communicationNodes.map { it.mapToUiElement() })
   elements.addAll(lines.map { it.mapToUiElement() })
   return elements
}

@Composable
fun ConfigDump.toApplicationState(): AppState {
   val elements = elements.mapToElements()
   return AppState(
       drawerContext = DrawerContext(
           elementsState = mutableStateOf(elements),
           selectedElementState = mutableStateOf(null),
           elementCounter = elements.maxByOrNull { it.id }?.id?.plus(1) ?: elements.size,
           messageState = mutableStateOf(null),
           simulationContext = SimulationContext(
              simulationStartedState = mutableStateOf(false),
              simulationStoppedState = mutableStateOf(false),
              simulationPathState = mutableStateOf(null)
           )
       )
   )
}
