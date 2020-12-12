package com.github.yandroidua.simulation.models

enum class LineType(val id: Int, val uiName: String) {
    DUPLEX(0, "Дуплексний"),
    HALF_DUPLEX(1, "Напівдуплексний"),
    SATELLITE(2, "Супутниковий");

    companion object {
        fun fromId(id: Int): LineType? {
            for (value in values()) {
                if (value.id == id) return value
            }
            return null
        }
    }

}