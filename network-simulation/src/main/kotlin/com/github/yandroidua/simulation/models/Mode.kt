package com.github.yandroidua.simulation.models

enum class Mode(val uiName: String) {
    VIRTUAL(uiName = "Режим віртуального канала"),
    DATAGRAM(uiName = "Дейтаграмний режим"),
    LOGICAL(uiName = "Режим встановлення логічного з'єднання")
}