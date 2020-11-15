package com.github.yandroidua.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.Element

@Composable
fun DetailsScreen(modifier: Modifier = Modifier, element: Element, saver: (Element) -> Unit) = Column(modifier) {
    Button(onClick = {}) { Text("Test1") }
    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
    Button(onClick = {}) { Text("Test2") }
    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
    Button(onClick = {}) { Text("Test3") }
    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
    Button(onClick = {}) { Text("Test4") }
}