package com.github.yandroidua.ui.models

import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.base.ConnectableElement

data class PathResultElements(
        val from: ConnectableElement,
        val to: ConnectableElement,
        val weight: Int,
        val path: List<Pair<ElementLine, ConnectableElement>>
)