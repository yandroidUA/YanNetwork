package com.github.yandroidua.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) = Column(modifier = modifier) {
    Text(text = "Settings page")
    Text(text = "Some settings#1")
    Text(text = "Some settings#2")
    Text(text = "Some settings#3")
    Text(text = "Some settings#4")
    Text(text = "Some settings#5")
}