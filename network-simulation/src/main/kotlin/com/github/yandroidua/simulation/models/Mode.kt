package com.github.yandroidua.simulation.models

enum class Mode(val uiName: String) {
    LOGICAL(uiName = "Режим встановлення логічного з'єднання"),
    VIRTUAL(uiName = "Режим вірнуального канала"),
    DATAGRAM(uiName = "Дейтаграмний режим")
}