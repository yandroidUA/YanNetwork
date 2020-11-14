package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset

class Workstation(
        val id: Int,
        offset: Offset
) : ImageControlElement(image = "workstation.png", offset) {

    override val height: Int = 32
    override val width: Int = 32

}