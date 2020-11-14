package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.yandroidua.ui.utils.StartEndOffset

data class Line(
        val startEndOffset: StartEndOffset,
        val firstStationId: Int,
        val secondStationId: Int,
        val color: Color,
        val state: State,
        val isInMovement: Boolean = false
) : Element {

    enum class State {
        CREATED,
        CREATING
    }

    override val center: Offset by lazy {
        Offset(
                x = (startEndOffset.startPoint.x + startEndOffset.endPoint.x) / 2f,
                y = (startEndOffset.startPoint.y + startEndOffset.endPoint.y) / 2f
        )
    }

    //todo fix to determine which of offsets will be start and which will be end
    private val rect: Rect by lazy {
        Rect(topLeft = startEndOffset.startPoint, bottomRight = startEndOffset.endPoint)
    }

    override fun isInOffset(offset: Offset): Boolean {
        val xA = startEndOffset.startPoint.x
        val yA = startEndOffset.startPoint.y
        val xB = startEndOffset.endPoint.x
        val yB = startEndOffset.endPoint.y
        val x = offset.x
        val y = offset.y
        return y == (x * (yB - yA) - xA * yB + xA * yA + yA * xB - yA * xA) / (xB - xA) && rect.contains(offset)
    }

    override val type: ElementType = ElementType.LINE

    override fun onDraw(drawScope: DrawScope) {
//        if (state == State.CREATING && !isInMovement) return
        drawScope.drawLine(color, startEndOffset.startPoint, startEndOffset.endPoint)
    }

}