package com.github.yandroidua.simulation.models

enum class LineType(val id: Int) {
    DUPLEX(0),
    HALF_DUPLEX(1),
    SATELLITE(2);

    companion object {
        fun fromId(id: Int): LineType? {
            for (value in values()) {
                if (value.id == id) return value
            }
            return null
        }
    }

}