package com.github.yandroidua.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.Element

@Composable
fun DetailsScreen(modifier: Modifier = Modifier, element: Element, saver: (Element) -> Unit) = Column(modifier.then(
        Modifier
                .background(Color.White)
                .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
                .padding(10.dp)
)) {
    Button(onClick = {}) { Text("Test1") }
    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
    Button(onClick = {}) { Text("Test2") }
    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
    Button(onClick = {}) { Text("Test3") }
    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
    Button(onClick = {}) { Text("Test4") }
}