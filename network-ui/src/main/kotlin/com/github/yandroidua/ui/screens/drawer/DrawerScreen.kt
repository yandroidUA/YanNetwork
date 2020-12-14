package com.github.yandroidua.ui.screens.drawer


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.mappers.mapToUiResult
import com.github.yandroidua.ui.models.TabType
import com.github.yandroidua.ui.screens.SimulationScreen
import com.github.yandroidua.ui.screens.calculation.CalculationWindow
import com.github.yandroidua.ui.screens.details.DetailsScreen
import com.github.yandroidua.ui.utils.DaeerTheming

// ------------------------------------Constants------------------------------------------------------------------------

private const val DETAILS_SCREEN_WIDTH = 350

private fun onDetailsShow(show: Boolean) {
//   val window = AppManager.windows.find { it.title == MAIN_WINDOW_TITLE } ?: return
//   window.setSize(
//      width = if (show && window.width <= WIDTH) {
//         WIDTH + DETAILS_SCREEN_WIDTH
//      } else if (show) {
//         if (window.width >= WIDTH + DETAILS_SCREEN_WIDTH)
//            window.width
//         else
//            WIDTH + DETAILS_SCREEN_WIDTH
//      } else WIDTH,
//      height = window.height
//   )
}

private fun calculate(context: DrawerContext, navigator: (TabType, Any?) -> Unit) {
    CalculationWindow(
            workstations = context.connectableElements,
            lines = context.lines,
            simulationContext = context.simulationContext
    ) { result ->
        val first = result.paths.map { it.mapToUiResult(context.elementsState.value) }.firstOrNull()
                ?: return@CalculationWindow
        context.simulationContext.simulationPathState.value = first
//      navigator(
//         TabType.RESULTS,
//         c
//          drawerContext?.apply {
//            simulationContext.simulationPathState.value = first
//         }?.also { applicationState.drawerContext = it }
//      )
    }
}

@Composable
fun PanelScreen(
        modifier: Modifier = Modifier,
        context: DrawerContext,
        onRestart: () -> Unit,
        navigator: (TabType, Any?) -> Unit
) = Row(modifier) {
    Row(modifier = Modifier.weight(1f)) {
        ControlPanel(context, navigator)
        DrawArea(
                modifier = Modifier.fillMaxSize().background(Color.White).weight(1f),
                elementsState = context.elementsState,
                onPointerMove = context::onMouseMoved,
                onTap = { offset ->
                    context.selectedElementState.value = context.onCanvasTyped(offset)
                }
        )
    }

    if (context.selectedElementState.value != null) {
        DetailsScreen(
                modifier = Modifier
                        .width(width = DETAILS_SCREEN_WIDTH.dp)
                        .fillMaxHeight(),
                element = context.selectedElementState.value!!,
                deleter = context::removeElement,
                saver = context::changeElement,
                connections = context.findConnections(context.selectedElementState.value!!),
                closer = { context.selectedElementState.value = null }
        )
    }
    if (context.simulationContext.simulationPathState.value != null) {
        SimulationScreen(
                modifier = Modifier
                        .width(width = DETAILS_SCREEN_WIDTH.dp)
                        .fillMaxHeight(),
                stopState = context.simulationContext.simulationStoppedState,
                simulationState = context.messageState,
                startState = context.simulationContext.simulationStartedState,
                path = context.simulationContext.simulationPathState.value!!,
                onRestart = onRestart,
                onNext = context::next,
                onStop = context::stop,
                onResume = context::resume,
                onClose = context::cancelAll
        )
    }
}


@Composable
private fun ControlPanel(context: DrawerContext, navigator: (TabType, Any?) -> Unit) = Column(
        modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .background(Color.White)
                .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
                .padding(start = 10.dp, end = 10.dp)
) {
    Column(modifier = Modifier.weight(weight = 1f)) {
        Spacer(modifier = Modifier.height(8.dp))
        Button(
                colors = DaeerTheming.buttonColors(),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                onClick = context::undo
        ) { Text(text = "Назад") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
                colors = DaeerTheming.buttonColors(),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                onClick = { context.cancel(); onDetailsShow(false) }) { Text(text = "Відмінити") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
                colors = DaeerTheming.buttonColors(),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                onClick = { calculate(context, navigator) }
        ) { Text(text = "Запустити симуляцію") }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(all = 4.dp)
                .border(color = Color.Black, shape = RectangleShape, width = 4.dp)
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Image(imageFromResource("workstation.png"),
                    modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .width(32.dp)
                            .height(32.dp)
                            .clickable { context.selectedElementType = ElementType.WORKSTATION }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Spacer(modifier = Modifier.height(height = 60.dp).width(width = 1.dp).background(color = Color.Black))
            Spacer(modifier = Modifier.width(10.dp))

            Image(imageFromResource("communication_node.png"),
                    modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .width(32.dp)
                            .height(32.dp)
                            .clickable { context.selectedElementType = ElementType.COMMUNICATION_NODE }
            )

            Spacer(modifier = Modifier.width(10.dp))
            Spacer(modifier = Modifier.height(height = 60.dp).width(width = 1.dp).background(color = Color.Black))
            Spacer(modifier = Modifier.width(10.dp))

            Image(imageFromResource("line.jpg"),
                    modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .width(32.dp)
                            .height(32.dp)
                            .clickable { context.selectedElementType = ElementType.LINE }
            )

            Spacer(modifier = Modifier.width(10.dp))
            Spacer(modifier = Modifier.height(height = 60.dp).width(width = 4.dp).background(color = Color.Black))
        }
    }
    Button(
            colors = DaeerTheming.buttonColors(),
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(bottom = 16.dp),
            onClick = context::clear
    ) { Text(text = "Очистити") }
}

@Composable
private fun DrawArea(
        modifier: Modifier = Modifier,
        elementsState: MutableState<List<Element>>,
        onPointerMove: (Offset) -> Boolean,
        onTap: (Offset) -> Unit
) {
    val horizontalScrollState = rememberScrollState(0f)
    val verticalScrollState = rememberScrollState(0f)
    val hController = rememberScrollbarAdapter(horizontalScrollState)
    val vController = rememberScrollbarAdapter(verticalScrollState)
    Column(
            modifier
                    .fillMaxSize()
                    .background(Color.White)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Canvas(
                    modifier = modifier
                            .verticalScroll(state = verticalScrollState, enabled = true)
                            .horizontalScroll(state = horizontalScrollState, enabled = true)
                            .width(width = 3420.dp)
                            .height(height = 1920.dp)
                            .weight(1f)
                            .pointerMoveFilter(onMove = onPointerMove, onEnter = { true })
                            .tapGestureFilter {
                                println("TAP")
                                onTap(it)
                            }
            ) {
                elementsState.value.forEach {
                    it.onDraw(this)
                }
            }
            VerticalScrollbar(adapter = vController)
        }
        HorizontalScrollbar(adapter = hController)
    }
}
