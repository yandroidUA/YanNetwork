package com.github.yandroidua.ui.elements.base

import com.github.yandroidua.ui.elements.ElementLine

interface ConnectableElement : Element {
    val lines: MutableList<ElementLine>
    override val connectable: Boolean
        get() = true
}