package com.github.yandroidua.ui

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.Element
import com.github.yandroidua.ui.screens.PanelScreen
import com.github.yandroidua.ui.screens.SettingsScreen

enum class TabType {
    SETTINGS,
    PANEL,
    RESULTS,
    DETAILS
}

const val MAIN_WINDOW_TITLE = "Andy Yan, KV71"

fun main() = Window(title = MAIN_WINDOW_TITLE, size = IntSize(800, 600)) {
    val selectedTabState = remember { mutableStateOf(TabType.SETTINGS) }
    val details = remember { mutableStateOf<Element?>(null) }

    Column(modifier = Modifier.fillMaxWidth().background(color = Color.Magenta)) {
        Row(modifier = Modifier
                .fillMaxWidth(fraction = 1f)
                .background(color = Color.Gray)
                .wrapContentHeight(align = Alignment.Top)
        ) {
            PageTab(
                    selected = selectedTabState.value == TabType.SETTINGS,
                    onClick = { selectedTabState.value = TabType.SETTINGS },
                    text = "Settings",
                    modifier = Modifier.weight(1/4f)
            )
            PageTab(
                    selected = selectedTabState.value == TabType.PANEL,
                    onClick = { selectedTabState.value = TabType.PANEL },
                    text = "PANEL",
                    modifier = Modifier.weight(1/4f)
            )
            PageTab(
                    selected = selectedTabState.value == TabType.RESULTS,
                    onClick = { selectedTabState.value = TabType.RESULTS },
                    text = "RESULTS",
                    modifier = Modifier.weight(1/4f)
            )
            if (details.value != null) {
                PageTab(
                        selected = selectedTabState.value == TabType.DETAILS,
                        onClick = { selectedTabState.value = TabType.DETAILS },
                        text = "DETAILS",
                        modifier = Modifier.weight(1 / 4f)
                )
            }
        }
        when (selectedTabState.value) {
            TabType.SETTINGS -> SettingsScreen()
            TabType.PANEL -> PanelScreen {
                val window = AppManager.windows.find { it.title == MAIN_WINDOW_TITLE } ?: return@PanelScreen
                window.setSize(width = 1000, height = window.height)
                details.value = it
            }
            TabType.RESULTS -> Text(text = "RESULTS")
            TabType.DETAILS -> Text(text = "DETAILS")
        }
    }
}

@Composable
private fun PageTab(selected: Boolean, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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