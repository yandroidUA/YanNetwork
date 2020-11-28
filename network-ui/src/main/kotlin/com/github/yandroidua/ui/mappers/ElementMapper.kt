package com.github.yandroidua.ui.mappers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.github.yandroidua.algorithm.Line
import com.github.yandroidua.algorithm.LineType
import com.github.yandroidua.algorithm.Workstation
import com.github.yandroidua.dump.models.*
import com.github.yandroidua.simulation.models.SimulationConnection
import com.github.yandroidua.simulation.models.SimulationModel
import com.github.yandroidua.simulation.models.SimulationWorkstation
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.utils.StartEndOffset

//-----------------------------------Algorithm models-------------------------------------------------------------------

fun Workstation.mapToUiElement(): ConnectableElement = ElementWorkstation(
        id = number,
        offset = Offset(0f, 0f),
        lineIds = linesId.toMutableList()
)

fun Line.mapToUiElement(): Element = ElementLine(
        id = id,
        secondStationId = station2Number,
        firstStationId = station1Number,
        weight = weight,
        startEndOffset = StartEndOffset(Offset(0f, 0f), Offset(0f, 0f)),
        color = Color.Black, //todo map by type
        state = ElementLine.State.CREATED
)

//------------------------------------Dump models-----------------------------------------------------------------------

fun WorkstationDump.mapToUiElement(): ConnectableElement = ElementWorkstation(
        id = id,
        offset = offset.mapToOffset(),
        lineIds = lineIds.toMutableList()
)

fun CommunicationNodeDump.mapToUiElement(): ConnectableElement = ElementCommunicationNode(
        id = id,
        offset = offset.mapToOffset(),
        lineIds = lineIds.toMutableList()
)

fun OffsetDump.mapToOffset(): Offset = Offset(x, y)

fun ColorDump.mapToColor(): Color = Color(red, green, blue, 0xFF)

fun LineDump.mapToUiElement(): Element = ElementLine(
        id = id,
        secondStationId = secondStationId,
        firstStationId = firstStationId,
        weight = weight,
        startEndOffset = StartEndOffset(startOffset.mapToOffset(), endOffset.mapToOffset()),
        color = color.mapToColor(),
        state = ElementLine.State.valueOf(state)
)

//------------------------------------UI models-------------------------------------------------------------------------

fun Element.mapToSimulation(): SimulationModel {
    return when(type) {
        ElementType.WORKSTATION, ElementType.COMMUNICATION_NODE -> (this as ConnectableElement).mapToSimulation()
        ElementType.LINE -> (this as ElementLine).mapToSimulation()
    }
}

fun ElementLine.mapToAlgorithmEntity(): Line = Line(
        id = id,
        station1Number = firstStationId,
        station2Number = secondStationId,
        weight = weight,
        type = LineType.DUPLEX, //todo change
        errorChance = 0.0f //todo change
)

fun ElementLine.mapToSimulation(): SimulationConnection = SimulationConnection(
        id = id,
        workstation1Id = firstStationId,
        workstation2Id = secondStationId,
        type = com.github.yandroidua.simulation.models.LineType.DUPLEX, //todo change
        weight = weight,
        errorChance = 0f //todo change
)

fun ConnectableElement.mapToSimulation(): SimulationWorkstation = SimulationWorkstation(
        id = id,
        connectionIds = lineIds
)

fun ConnectableElement.mapToAlgorithmEntity(): Workstation = Workstation(
        number = id,
        linesId = lineIds
)

fun Offset.mapToDump(): OffsetDump = OffsetDump(x, y)

fun Color.mapToDump(): ColorDump = ColorDump(red.toInt(), green.toInt(), blue.toInt(), alpha.toInt())

fun ElementWorkstation.mapToDump(): WorkstationDump = WorkstationDump(
        id = id,
        offset = offset.mapToDump(),
        lineIds = lineIds
)

fun ElementCommunicationNode.mapToDump(): CommunicationNodeDump = CommunicationNodeDump(
        id = id,
        offset = offset.mapToDump(),
        lineIds = lineIds
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
        isInMovement = isInMovement
)
