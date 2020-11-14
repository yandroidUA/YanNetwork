package com.github.yandroidua.ui.utils

import androidx.compose.ui.geometry.Offset
import com.github.yandroidua.ui.elements.Line

data class LinesState(
        val lines: List<Line>,
        val dotsBefore: List<Offset>
) : UndoableState {

    override fun undo() {
        if (lines.isEmpty()) return

    }

}