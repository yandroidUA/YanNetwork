package com.github.yandroidua.ui.mappers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.github.yandroidua.algorithm.Line
import com.github.yandroidua.algorithm.LineType
import com.github.yandroidua.algorithm.Workstation
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.utils.StartEndOffset

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

fun ElementLine.mapToAlgorithmEntity(): Line = Line(
        id = id,
        station1Number = firstStationId,
        station2Number = secondStationId,
        weight = weight,
        type = LineType.DUPLEX, //todo change
        errorChance = 0.0f //todo change
)

fun ConnectableElement.mapToAlgorithmEntity(): Workstation = Workstation(
        number = id,
        linesId = lineIds
)