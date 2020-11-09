package com.github.yandroidua.ui

import androidx.compose.desktop.Window
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize

fun main() = Window(title = "Andy Yan, KV71", size = IntSize(300, 300)) {
    val state = remember { mutableStateOf(0) }
    Button(
            onClick = { state.value += 1 },
    ) {
        Text(text = "Count: ${state.value}")
    }
}