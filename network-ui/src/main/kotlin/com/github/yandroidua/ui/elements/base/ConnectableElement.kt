package com.github.yandroidua.ui.elements.base

interface ConnectableElement : Element {
    val lineIds: MutableList<Int>
    val network: String?
    override val connectable: Boolean
        get() = true
}