package com.github.yandroidua.ui.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.DrawScope

abstract class DrawableElement {

//    @Composable
//    abstract fun onStateInitialize()

    abstract fun onDraw(drawScope: DrawScope)

}