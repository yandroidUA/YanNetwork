package com.github.yandroidua.ui

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
import com.github.yandroidua.ui.screens.PanelPageContext
import com.github.yandroidua.ui.screens.PanelScreen
import com.github.yandroidua.ui.screens.SettingsScreen

enum class TabType {
    SETTINGS,
    PANEL,
    RESULTS,
    DETAILS
}

const val MAIN_WINDOW_TITLE = "Andy Yan, KV71"
const val WIDTH = 800
const val HEIGHT = 600

data class AppState(
        var panelScreenContextPanel: PanelPageContext? = null
)

private val applicationState = AppState()

fun main() = Window(title = MAIN_WINDOW_TITLE, size = IntSize(WIDTH, HEIGHT)) {
    val selectedTabState = remember { mutableStateOf(TabType.SETTINGS) }

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
        }
        when (selectedTabState.value) {
            TabType.SETTINGS -> SettingsScreen()
            TabType.PANEL -> PanelScreen(pageContext = applicationState.panelScreenContextPanel ?: PanelPageContext(
                    elementsState = remember { mutableStateOf(emptyList()) },
                    selectedElementState = remember { mutableStateOf(null) }
            ).also { applicationState.panelScreenContextPanel = it })
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