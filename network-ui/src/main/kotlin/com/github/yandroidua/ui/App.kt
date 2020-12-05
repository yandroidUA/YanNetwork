package com.github.yandroidua.ui

import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.github.yandroidua.dump.Dumper
import com.github.yandroidua.ui.components.PageTab
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.models.PathCalculationResult
import com.github.yandroidua.ui.models.TabType
import com.github.yandroidua.ui.screens.ResultScreen
import com.github.yandroidua.ui.screens.SettingsScreen
import com.github.yandroidua.ui.screens.SettingsState
import com.github.yandroidua.ui.screens.drawer.DrawerContext
import com.github.yandroidua.ui.screens.drawer.PanelScreen
import com.github.yandroidua.ui.screens.drawer.SimulationContext
import com.github.yandroidua.ui.utils.addToDumpElements
import com.github.yandroidua.ui.utils.toApplicationState
import kotlinx.coroutines.Dispatchers

//-----------------------------------Constants--------------------------------------------------------------------------

const val MAIN_WINDOW_TITLE = "Andy Yan, KV71"
const val WIDTH = 900
const val HEIGHT = 700
private const val TITLE_SETTINGS = "Settings"
private const val TITLE_PANEL = "Drawer"
private const val TITLE_RESULTS = "Results"

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
          simulationStoppedState = mutableStateOf(false)
       )
   )
}

@Composable
private fun createEmptySettingsState(): SettingsState {
   return SettingsState(dumpPathState = mutableStateOf("D:\\Projects\\test.txt"))
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
   val navigationState: MutableState<Pair<TabType, Any?>> = remember { mutableStateOf(TabType.SETTINGS to null) }

   Column(modifier = Modifier.fillMaxWidth()) {
      Row(modifier = Modifier
          .fillMaxWidth(fraction = 1f)
          .background(color = Color.Gray)
          .wrapContentHeight(align = Alignment.Top)
      ) {
         PageTab(
             selected = isCurrentNavigation(navigationState, TabType.SETTINGS),
             onClick = { navigateTo(navigationState, TabType.SETTINGS) },
             text = TITLE_SETTINGS,
             modifier = Modifier.weight(1 / 3f)
         )
         PageTab(
             selected = isCurrentNavigation(navigationState, TabType.PANEL),
             onClick = { navigateTo(navigationState, TabType.PANEL, applicationState.drawerContext) },
             text = TITLE_PANEL,
             modifier = Modifier.weight(1 / 3f)
         )
         PageTab(
             selected = isCurrentNavigation(navigationState, TabType.RESULTS),
             onClick = { navigateTo(navigationState, TabType.RESULTS) },
             text = TITLE_RESULTS,
             modifier = Modifier.weight(1 / 3f)
         )
      }
      MainNavigator(navigationState)
   }
}

@Composable
private fun MainNavigator(navigationState: MutableState<Pair<TabType, Any?>>) {
   val arguments = navigationState.value.second
   when (navigationState.value.first) {
       TabType.SETTINGS -> createSettings(applicationState.settingsState, navigationState)
       TabType.PANEL -> createPanelScreen(
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
    argDrawerContext: DrawerContext?,
    navigator: (TabType, Any?) -> Unit
) {
   val drawerContext = argDrawerContext ?: createEmptyDrawerContext()
   applicationState.drawerContext = drawerContext
   PanelScreen(
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
              simulationContext.simulationPath = simulationPath
          }?.also { applicationState.drawerContext = it }
      )
   }
}

@Composable
private fun createSettings(
    settingsState: SettingsState?,
    navigationState: MutableState<Pair<TabType, Any?>>
) {
   val state = settingsState ?: createEmptySettingsState()
   applicationState.settingsState = state
   SettingsScreen(
       settingsState = state,
       dumper = { path -> dumpState(path) },
       loader = { path -> loadStateFromDump(path, navigationState) }
   )
}
