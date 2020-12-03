package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DesktopCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.models.StartEndOffset
import org.jetbrains.skija.Font
import org.jetbrains.skija.Typeface
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class ElementLine(
        override val id: Int,
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

    override val connectable: Boolean = false

    private val rect: Rect by lazy {
        val leftX = minOf(startEndOffset.startPoint.x, startEndOffset.endPoint.x)
        val topY = minOf(startEndOffset.startPoint.y, startEndOffset.endPoint.y)
        val rightX = maxOf(startEndOffset.startPoint.x, startEndOffset.endPoint.x)
        val bottomY = maxOf(startEndOffset.startPoint.y, startEndOffset.endPoint.y)
        Rect(topLeft = Offset(leftX - LINE_THRESHOLD / 2f, topY - LINE_THRESHOLD / 2f), bottomRight = Offset(rightX + LINE_THRESHOLD / 2f, bottomY + LINE_THRESHOLD / 2f))
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

    fun nextOffset(xOffset: Float, currentTime: Int, time: Int, reverse: Boolean = true): Offset {
        val distance = sqrt(
                (startEndOffset.startPoint.x.toDouble() - startEndOffset.endPoint.x).pow(2.0) +
                        (startEndOffset.startPoint.y.toDouble() - startEndOffset.endPoint.y).pow(2.0)
        )
        val currentX = xOffset + if (reverse) (distance * (1 - currentTime / time.toFloat())).toFloat() else (distance * currentTime / time).toFloat()
        val currentY = f(currentX)
        return Offset(currentX, currentY)
    }

    fun f(x: Float): Float {
        val xA = startEndOffset.startPoint.x
        val yA = startEndOffset.startPoint.y
        val xB = startEndOffset.endPoint.x
        val yB = startEndOffset.endPoint.y
        return (((x * (yB - yA) - xA * yB + xA * yA + yA * xB - yA * xA)) / (xB - xA))
    }

    override fun isInOffset(offset: Offset): Boolean {
        val x = offset.x
        val y = offset.y
        return abs(y - f(x)) <= LINE_THRESHOLD
                && rect.contains(offset)
    }

    override fun onDraw(drawScope: DrawScope) {
        drawScope.drawLine(color, startEndOffset.startPoint, startEndOffset.endPoint)
        drawScope.drawIntoCanvas { d ->
            (d as DesktopCanvas).skija.drawString("$weight", center.x, center.y, skiaFont, paint.asFrameworkPaint())
        }
    }

}