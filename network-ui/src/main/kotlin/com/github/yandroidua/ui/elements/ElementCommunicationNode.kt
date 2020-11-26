package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DesktopCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.elements.base.ImageControlElement
import org.jetbrains.skija.Font
import org.jetbrains.skija.Typeface

class ElementCommunicationNode(
        override val id: Int,
        offset: Offset,
        override val lineIds: MutableList<Int> = mutableListOf()
) : ImageControlElement(image = "communication_node.png", offset), ConnectableElement {

    override val height: Int = 24
    override val width: Int = 24

    override val type: ElementType = ElementType.COMMUNICATION_NODE

    private val paint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.Blue
        }
    }

    private val skiaFont: Font by lazy {
        Font(Typeface.makeDefault(), 15f)
    }

    override fun onDraw(drawScope: DrawScope) {
        super.onDraw(drawScope)
        drawScope.drawIntoCanvas { canvas ->
            (canvas as DesktopCanvas).skija.drawString("$id", center.x, center.y - height / 2f, skiaFont, paint.asFrameworkPaint())
        }
    }

}