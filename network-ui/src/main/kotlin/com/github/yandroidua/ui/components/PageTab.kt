package com.github.yandroidua.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

@Composable
fun PageTab(
        selected: Boolean,
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Surface(
            color = if (selected) Color.Yellow else Color.White,
            shape = RoundedCornerShape(topLeft = 10.dp, topRight = 10.dp),
            modifier = modifier.then(Modifier
                    .border(width = 1.dp, color = Color.Blue, shape = RoundedCornerShape(topLeft = 10.dp, topRight = 10.dp)))
                    .background(Color.White)
                    .clickable { onClick() }
    ) {
        Text(
                text = text,
                modifier = Modifier
                        .wrapContentSize(align = Alignment.Center),
                fontStyle = if (selected) FontStyle.Italic else FontStyle.Normal,
        )
    }

}