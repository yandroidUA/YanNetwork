package com.github.yandroidua.ui.mappers

import com.github.yandroidua.algorithm.PathResult
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.utils.PathResultElements

fun PathResult.mapToUiResult(
        elements: List<Element>
): PathResultElements = PathResultElements(
        from = elements.find { it.id == from } as ConnectableElement,
        to = elements.find { it.id == to } as ConnectableElement,
        path = path.map { oldP ->
            elements.find { it.id == oldP.first } as ElementLine to elements.find { it.id == oldP.second } as ConnectableElement
        },
        weight = summary
)