package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

interface Element {

    val type: ElementType

    val center: Offset

    fun isInOffset(offset: Offset): Boolean

    fun onDraw(drawScope: DrawScope)

}