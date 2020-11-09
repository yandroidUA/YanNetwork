package com.github.yandroidua.ui.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.yandroidua.ui.utils.Coordinates

data class Line(
        val coordinates: Coordinates,
        val color: Color
) : DrawableElement() {

//    private var colorState: MutableState<Color>? = null
//    private var coordinatesState: MutableState<Coordinates>? = null
//
//    @Composable
//    override fun onStateInitialize() {
//        if (colorState == null) {
//            colorState = remember { mutableStateOf(color) }
//        }
//        if (coordinatesState == null) {
//            coordinatesState = remember { mutableStateOf(coordinates) }
//        }
//    }

    override fun onDraw(drawScope: DrawScope) {
//        val color = colorState?.value ?: Color.Black
//        val coordinates = coordinatesState?.value ?: return
        drawScope.drawLine(color, coordinates.startPoint, coordinates.endPoint)
    }

}