package com.github.yandroidua.ui.mappers

import com.github.yandroidua.algorithm.Line
import com.github.yandroidua.algorithm.LineType
import com.github.yandroidua.algorithm.Workstation
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.base.ConnectableElement

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