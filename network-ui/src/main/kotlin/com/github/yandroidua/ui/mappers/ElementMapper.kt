package com.github.yandroidua.ui.mappers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.github.yandroidua.algorithm.models.Line
import com.github.yandroidua.algorithm.models.Workstation
import com.github.yandroidua.dump.models.*
import com.github.yandroidua.simulation.models.*
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.models.StartEndOffset

//------------------------------------Dump models-----------------------------------------------------------------------

fun WorkstationDump.mapToUiElement(): ConnectableElement = ElementWorkstation(
    id = id,
    offset = offset.mapToOffset(),
    lineIds = lineIds.toMutableList(),
    network = network
)

fun CommunicationNodeDump.mapToUiElement(): ConnectableElement = ElementCommunicationNode(
    id = id,
    offset = offset.mapToOffset(),
    lineIds = lineIds.toMutableList(),
    network = network
)

fun OffsetDump.mapToOffset(): Offset = Offset(x, y)

fun ColorDump.mapToColor(): Color = Color(red, green, blue, alpha)

fun LineDump.mapToUiElement(): Element = ElementLine(
    id = id,
    secondStationId = secondStationId,
    firstStationId = firstStationId,
    weight = weight,
    startEndOffset = StartEndOffset(startOffset.mapToOffset(), endOffset.mapToOffset()),
    color = color.mapToColor(),
    lineType = LineType.fromId(type) ?: LineType.DUPLEX,
    errorChance = errorChance,
    state = ElementLine.State.valueOf(state)
)

//------------------------------------UI models-------------------------------------------------------------------------

fun Element.mapToSimulation(): SimulationModel? {
   return when (type) {
       ElementType.WORKSTATION, ElementType.COMMUNICATION_NODE -> (this as ConnectableElement).mapToSimulation()
       ElementType.LINE -> (this as ElementLine).mapToSimulation()
      else -> null
   }
}

fun ElementLine.mapToAlgorithmEntity(): Line = Line(
    id = id,
    station1Number = firstStationId,
    station2Number = secondStationId,
    weight = weight
)

fun ElementLine.mapToSimulation(): SimulationConnection = SimulationConnection(
    id = id,
    workstation1Id = firstStationId,
    workstation2Id = secondStationId,
    type = lineType,
    weight = weight,
    errorChance = errorChance
)

fun ConnectableElement.mapToSimulation(): SimulationWorkstation = SimulationWorkstation(
    id = id,
    connectionIds = lineIds,
    network = network,
    isWorkstation = this is ElementWorkstation
)

fun ConnectableElement.mapToAlgorithmEntity(): Workstation = Workstation(
    number = id,
    linesId = lineIds
)

fun Offset.mapToDump(): OffsetDump = OffsetDump(x, y)

fun Color.mapToDump(): ColorDump = ColorDump(red, green, blue, alpha)

fun ElementWorkstation.mapToDump(): WorkstationDump = WorkstationDump(
    id = id,
    offset = offset.mapToDump(),
    lineIds = lineIds,
    network = network
)

fun ElementCommunicationNode.mapToDump(): CommunicationNodeDump = CommunicationNodeDump(
    id = id,
    offset = offset.mapToDump(),
    lineIds = lineIds,
    network = network
)

fun ElementLine.mapToDump(): LineDump = LineDump(
    id = id,
    secondStationId = secondStationId,
    firstStationId = firstStationId,
    weight = weight,
    startOffset = startEndOffset.startPoint.mapToDump(),
    endOffset = startEndOffset.endPoint.mapToDump(),
    color = color.mapToDump(),
    state = state.name,
    type = lineType.id,
    errorChance = errorChance,
    isInMovement = isInMovement
)
