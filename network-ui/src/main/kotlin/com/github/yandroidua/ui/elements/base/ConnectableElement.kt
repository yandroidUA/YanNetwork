package com.github.yandroidua.ui.elements.base

interface ConnectableElement : Element {
    val lineIds: MutableList<Int>
    override val connectable: Boolean
        get() = true
}