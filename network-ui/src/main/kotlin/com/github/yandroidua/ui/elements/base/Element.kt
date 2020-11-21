package com.github.yandroidua.ui.elements.base

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

interface Element {

    val id: Int

    val type: ElementType

    val center: Offset

    val connectable: Boolean

    fun isInOffset(offset: Offset): Boolean

    fun onDraw(drawScope: DrawScope)

}