package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.Element
import com.github.yandroidua.ui.elements.ElementType
import com.github.yandroidua.ui.elements.Line
import com.github.yandroidua.ui.elements.Workstation

@Composable
fun DetailsScreen(modifier: Modifier = Modifier, element: Element, saver: (Element) -> Unit) = Column(modifier.then(
        Modifier
                .background(Color.White)
                .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
                .padding(10.dp)
)) {
    when (element.type) {
        ElementType.WORKSTATION -> WorkstationDetails(workstation = element as Workstation)
        ElementType.LINE -> LineDetails(line = element as Line)
    }
}


