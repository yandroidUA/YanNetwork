package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.elements.base.ImageControlElement

class ElementMessage(
        override val id: Int,
        offset: Offset
) : ImageControlElement("message.png", offset) {

    override val height: Int = 24
    override val width: Int = 24

    override val connectable: Boolean
        get() = false

    override val type: ElementType
        get() = ElementType.MESSAGE

    override fun onDraw(drawScope: DrawScope) {
        super.onDraw(drawScope)
//        println("Drawing message on $offset")
    }

}