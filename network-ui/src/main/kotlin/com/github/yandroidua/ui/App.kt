package com.github.yandroidua.ui

import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.github.yandroidua.ui.components.PageTab
import com.github.yandroidua.ui.screens.PanelPageContext
import com.github.yandroidua.ui.screens.PanelScreen
import com.github.yandroidua.ui.screens.SettingsScreen
import com.github.yandroidua.ui.utils.TabType

//-----------------------------------Constants--------------------------------------------------------------------------

const val MAIN_WINDOW_TITLE = "Andy Yan, KV71"
const val WIDTH = 900
const val HEIGHT = 700
private const val TITLE_SETTINGS = "Settings"
private const val TITLE_PANEL = "Drawer"
private const val TITLE_RESULTS = "Results"

//------------------------------------AppState--------------------------------------------------------------------------

data class AppState(
        var panelScreenContextPanel: PanelPageContext? = null
)

//-----------------------------------Utils functions--------------------------------------------------------------------

@Composable
private fun createEmptyPageContextState(): PanelPageContext {
    return PanelPageContext(
            elementsState = remember { mutableStateOf(emptyList()) },
            selectedElementState = remember { mutableStateOf(null) }
    )
}

private fun navigateTo(state: MutableState<Pair<TabType, Any?>>, tabType: TabType, args: Any? = null) {
    state.value = tabType to args
}

private fun isCurrentNavigation(state: MutableState<Pair<TabType, Any?>>, direction: TabType): Boolean {
    return state.value.first == direction
}

//-----------------------------------UI---------------------------------------------------------------------------------

private val applicationState = AppState()

fun main() = Window(
        title = MAIN_WINDOW_TITLE,
        size = IntSize(WIDTH, HEIGHT)
) {
    val navigationState: MutableState<Pair<TabType, Any?>> = remember { mutableStateOf(TabType.SETTINGS to null) }

    Column(modifier = Modifier.fillMaxWidth().background(color = Color.Magenta)) {
        Row(modifier = Modifier
                .fillMaxWidth(fraction = 1f)
                .background(color = Color.Gray)
                .wrapContentHeight(align = Alignment.Top)
        ) {
            PageTab(
                    selected = isCurrentNavigation(navigationState, TabType.SETTINGS),
                    onClick = { navigateTo(navigationState, TabType.SETTINGS) },
                    text = TITLE_SETTINGS,
                    modifier = Modifier.weight(1/3f)
            )
            PageTab(
                    selected = isCurrentNavigation(navigationState, TabType.PANEL),
                    onClick = { navigateTo(navigationState, TabType.PANEL, applicationState.panelScreenContextPanel) },
                    text = TITLE_PANEL,
                    modifier = Modifier.weight(1/3f)
            )
            PageTab(
                    selected = isCurrentNavigation(navigationState, TabType.RESULTS),
                    onClick = { navigateTo(navigationState, TabType.RESULTS) },
                    text = TITLE_RESULTS,
                    modifier = Modifier.weight(1/3f)
            )
        }
        MainNavigator(navigationState)
    }
}

@Composable
private fun MainNavigator(navigationState: MutableState<Pair<TabType, Any?>>) {
    val arguments = navigationState.value.second
    when (navigationState.value.first) {
        TabType.SETTINGS -> SettingsScreen()
        TabType.PANEL -> createPanelScreen(arguments as? PanelPageContext) { tabType: TabType, argument: Any? ->
            navigateTo(navigationState, tabType, argument)
        }
        TabType.RESULTS -> Text(text = "RESULTS")
    }
}

@Composable
private fun createPanelScreen(
        panelPageContext: PanelPageContext?,
        navigator: (TabType, Any?) -> Unit
) {
    val pageContext = panelPageContext ?: createEmptyPageContextState()
    applicationState.panelScreenContextPanel = pageContext
    PanelScreen(pageContext = pageContext, navigator = navigator)
}