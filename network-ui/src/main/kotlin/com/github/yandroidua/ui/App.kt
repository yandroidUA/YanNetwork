package com.github.yandroidua.ui

import androidx.compose.desktop.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.github.yandroidua.ui.elements.Line
import com.github.yandroidua.ui.utils.Coordinates

fun main() = Window(title = "Andy Yan, KV71", size = IntSize(300, 300)) {
    val linesState = remember { mutableStateOf<List<Line>>(emptyList()) }
    var prevOffset: Offset? = null
    val stateCreator = object : StateCreator<Int> {
        @Composable
        override fun <S> createState(value: S): MutableState<S> {
            return remember { mutableStateOf(value) }
        }
    }

    Canvas(modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
            .tapGestureFilter {
                println("tapGestureFilter")
                if (prevOffset != null) {
                    linesState.value = linesState.value.toMutableList().apply {
                        add(Line(color = Color.Blue, coordinates = Coordinates(prevOffset!!, it)))
                    }
                }
                //todo create line with own state
                prevOffset = it
            }
    ) {
        linesState.value.forEach {
            it.onDraw(this)
        }
    }
}

interface StateCreator<S> {
    @Composable
    fun <S> createState(value: S): MutableState<S>
}