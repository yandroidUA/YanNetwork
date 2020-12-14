package com.github.yandroidua.ui

import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yandroidua.dump.Dumper
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.models.PathCalculationResult
import com.github.yandroidua.ui.models.TabType
import com.github.yandroidua.ui.screens.ResultScreen
import com.github.yandroidua.ui.screens.SettingsScreen
import com.github.yandroidua.ui.screens.SettingsState
import com.github.yandroidua.ui.screens.drawer.DrawerContext
import com.github.yandroidua.ui.screens.drawer.PanelScreen
import com.github.yandroidua.ui.screens.drawer.SimulationContext
import com.github.yandroidua.ui.utils.DaeerTheming
import com.github.yandroidua.ui.utils.addToDumpElements
import com.github.yandroidua.ui.utils.toApplicationState

//-----------------------------------Constants--------------------------------------------------------------------------

const val MAIN_WINDOW_TITLE = "Julya Tkachenko KV 72"
const val WIDTH = 1200
const val HEIGHT = 900
private const val TITLE_SETTINGS = "Збереження мережі"
private const val TITLE_PANEL = "Модифікація мережі"
private const val TITLE_RESULTS = "Результати"

//------------------------------------AppState--------------------------------------------------------------------------

data class AppState(
        var drawerContext: DrawerContext? = null,
        var results: PathCalculationResult? = null,
        var settingsState: SettingsState? = null
)

//-----------------------------------Utils functions--------------------------------------------------------------------

@Composable
private fun createEmptyDrawerContext(): DrawerContext {
    return DrawerContext(
            elementsState = mutableStateOf(emptyList()),
            selectedElementState = mutableStateOf(null),
            messageState = mutableStateOf(null),
            simulationContext = SimulationContext(
                    simulationStartedState = mutableStateOf(false),
                    simulationStoppedState = mutableStateOf(false),
                    simulationPathState = mutableStateOf(null)
            )
    )
}

@Composable
private fun createEmptySettingsState(): SettingsState {
    return SettingsState(dumpPathState = mutableStateOf("C:\\Users\\Daeer\\Desktop\\AAA\\test.txt"))

}

@Composable
private fun loadStateFromDump(path: String, navigationState: MutableState<Pair<TabType, Any?>>) {
    Dumper.EasyDumper().path(path).read()?.toApplicationState()?.let {
        applicationState.drawerContext = it.drawerContext
        applicationState.results = it.results
    }
    navigateTo(navigationState, navigationState.value.first, navigationState.value.second)
}

private fun navigateTo(state: MutableState<Pair<TabType, Any?>>, tabType: TabType, args: Any? = null) {
    state.value = tabType to args
}

private fun isCurrentNavigation(state: MutableState<Pair<TabType, Any?>>, direction: TabType): Boolean {
    return state.value.first == direction
}

private fun dumpState(path: String) {
    applicationState.drawerContext?.let {
        val d = Dumper.EasyDumper()
                .path(path = path)
                .addToDumpElements(it.elementsState.value)

        d.dump()
    }
}

//-----------------------------------UI---------------------------------------------------------------------------------

private val applicationState = AppState()

fun main() = Window(
        title = MAIN_WINDOW_TITLE,
        size = IntSize(WIDTH, HEIGHT)
) {
    MaterialTheme {
        val navigationState: MutableState<Pair<TabType, Any?>> = remember { mutableStateOf(TabType.SETTINGS to null) }
        lightColors(primary = Color.Magenta, primaryVariant = Color.Magenta)
        Row(
                modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White)
        ) {
//         Row(modifier = Modifier.fillMaxHeight().weight(1f).background(Color.Red)) {}
//         Row(modifier = Modifier.fillMaxHeight().width(width = 220.dp).background(Color.Green)) {}
            MainNavigator(modifier = Modifier.fillMaxHeight().weight(1f), navigationState)
            Spacer(modifier = Modifier.fillMaxHeight().width(width = 2.dp).background(Color.Black))
            Surface(modifier = Modifier.fillMaxHeight()) {
                Column(
                        modifier = Modifier
                                .width(width = 220.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .align(alignment = Alignment.CenterVertically)
                ) {
                    NavigationButton(
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                            selected = isCurrentNavigation(navigationState, TabType.SETTINGS),
                            onClick = { navigateTo(navigationState, TabType.SETTINGS) },
                            text = TITLE_SETTINGS
                    )
                    Spacer(modifier = Modifier.height(height = 8.dp))
                    NavigationButton(
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                            selected = isCurrentNavigation(navigationState, TabType.PANEL),
                            onClick = { navigateTo(navigationState, TabType.PANEL, applicationState.drawerContext) },
                            text = TITLE_PANEL
                    )
//               Spacer(modifier = Modifier.height(height = 8.dp))
//               NavigationButton(
//                  modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
//                  selected = isCurrentNavigation(navigationState, TabType.RESULTS),
//                  onClick = { navigateTo(navigationState, TabType.RESULTS) },
//                  text = TITLE_RESULTS
//               )
                }
            }

        }
    }
}

@Composable
private fun NavigationButton(modifier: Modifier = Modifier, onClick: () -> Unit, selected: Boolean, text: String) {
    Button(
            enabled = !selected,
            colors = DaeerTheming.buttonColors(),
            modifier = modifier.then(
                    Modifier
                            .padding(all = 10.dp)
                            .width(200.dp)
            ),
            onClick = onClick
    ) {
        Text(text = text)
    }
}

@Composable
private fun MainNavigator(modifier: Modifier = Modifier, navigationState: MutableState<Pair<TabType, Any?>>) {
    val arguments = navigationState.value.second
    when (navigationState.value.first) {
        TabType.SETTINGS -> createSettings(applicationState.settingsState, navigationState, modifier)
        TabType.PANEL -> createPanelScreen(
                modifier = modifier,
                argDrawerContext = arguments as? DrawerContext
        ) { tabType: TabType, argument: Any? ->
            navigateTo(navigationState, tabType, argument)
        }
        TabType.RESULTS -> createResults(
                results = arguments as? PathCalculationResult,
                elements = applicationState.drawerContext?.elementsState?.value ?: emptyList(),
                navigationState = navigationState
        )
    }
}

@Composable
private fun createPanelScreen(
        modifier: Modifier = Modifier,
        argDrawerContext: DrawerContext?,
        navigator: (TabType, Any?) -> Unit
) {
    val drawerContext = argDrawerContext ?: createEmptyDrawerContext()
    applicationState.drawerContext = drawerContext
    PanelScreen(
            modifier = modifier,
            context = drawerContext,
            navigator = navigator,
            onRestart = { drawerContext.startSimulation() }
    )
}

@Composable
private fun createResults(
        results: PathCalculationResult?,
        elements: List<Element>,
        navigationState: MutableState<Pair<TabType, Any?>>,
) {
    val res = results ?: applicationState.results
    applicationState.results = res
    ResultScreen(result = res, elements = elements) { simulationPath ->
        navigateTo(
                state = navigationState,
                tabType = TabType.PANEL,
                args = applicationState.drawerContext?.apply {
                    simulationContext.simulationPathState.value = simulationPath
                }?.also { applicationState.drawerContext = it }
        )
    }
}

@Composable
private fun createSettings(
        settingsState: SettingsState?,
        navigationState: MutableState<Pair<TabType, Any?>>,
        modifier: Modifier = Modifier
) {
    val state = settingsState ?: createEmptySettingsState()
    applicationState.settingsState = state
    SettingsScreen(
            modifier = modifier,
            settingsState = state,
            dumper = { path -> dumpState(path) },
            loader = { path -> loadStateFromDump(path, navigationState) }
    )
}
