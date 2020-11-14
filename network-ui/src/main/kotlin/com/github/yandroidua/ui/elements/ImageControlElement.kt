package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.unit.IntOffset

abstract class ImageControlElement(
        val image: String,
        val offset: Offset
) : Element {

    abstract val width: Int
    abstract val height: Int

    private val rect: Rect by lazy {
        Rect(topLeft = offset, bottomRight = Offset(x = offset.x + width, y = offset.y + height))
    }

    override val type: ElementType = ElementType.WORKSTATION

    override val center: Offset by lazy {
        Offset(x = offset.x + width / 2f, y = offset.y + height / 2f)
    }
    //todo make better collision
    override fun isInOffset(offset: Offset): Boolean {
        return rect.contains(offset)
    }

    override fun onDraw(drawScope: DrawScope) {
        drawScope.drawImage(
                image = imageFromResource(image),
                dstOffset = IntOffset(offset.x.toInt(), offset.y.toInt())
        )
    }

}