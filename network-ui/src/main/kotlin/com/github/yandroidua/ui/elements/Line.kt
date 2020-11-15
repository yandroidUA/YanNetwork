package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DesktopCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.github.yandroidua.ui.utils.StartEndOffset
import org.jetbrains.skija.Font
import org.jetbrains.skija.Typeface
import kotlin.math.abs

data class Line(
        val startEndOffset: StartEndOffset,
        val firstStationId: Int,
        val secondStationId: Int,
        val color: Color,
        val state: State,
        val weight: Int = 1,
        val isInMovement: Boolean = false
) : Element {

    companion object {
        private const val LINE_THRESHOLD = 4
    }

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
    private val rect: Rect by lazy {
        val leftX = minOf(startEndOffset.startPoint.x, startEndOffset.endPoint.x)
        val topY = minOf(startEndOffset.startPoint.y, startEndOffset.endPoint.y)
        val rightX = maxOf(startEndOffset.startPoint.x, startEndOffset.endPoint.x)
        val bottomY = maxOf(startEndOffset.startPoint.y, startEndOffset.endPoint.y)
        Rect(topLeft = Offset(leftX, topY), bottomRight = Offset(rightX, bottomY))
    }
    private val paint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.Blue
        }
    }
    private val skiaFont: Font by lazy {
        Font(Typeface.makeDefault(), 20f)
    }

    override val type: ElementType = ElementType.LINE

    override fun isInOffset(offset: Offset): Boolean {
        val xA = startEndOffset.startPoint.x
        val yA = startEndOffset.startPoint.y
        val xB = startEndOffset.endPoint.x
        val yB = startEndOffset.endPoint.y
        val x = offset.x
        val y = offset.y
        return abs(y - (((x * (yB - yA) - xA * yB + xA * yA + yA * xB - yA * xA)) / (xB - xA))) <= LINE_THRESHOLD
                && rect.contains(offset)
    }

    override fun onDraw(drawScope: DrawScope) {
        drawScope.drawLine(color, startEndOffset.startPoint, startEndOffset.endPoint)
        drawScope.drawIntoCanvas { d ->
            (d as DesktopCanvas).skija.drawString("$weight", center.x, center.y, skiaFont, paint.asFrameworkPaint())
        }
    }

}