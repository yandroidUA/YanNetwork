package com.github.yandroidua.ui.screens.drawer


import androidx.compose.desktop.AppManager
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
import com.github.yandroidua.ui.MAIN_WINDOW_TITLE
import com.github.yandroidua.ui.WIDTH
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.models.TabType
import com.github.yandroidua.ui.screens.calculation.CalculationWindow
import com.github.yandroidua.ui.screens.SimulationScreen
import com.github.yandroidua.ui.screens.details.DetailsScreen

// ------------------------------------Constants------------------------------------------------------------------------

private const val DETAILS_SCREEN_WIDTH = 350

private fun onDetailsShow(show: Boolean) {
   val window = AppManager.windows.find { it.title == MAIN_WINDOW_TITLE } ?: return
   window.setSize(
      width = if (show && window.width <= WIDTH) {
         WIDTH + DETAILS_SCREEN_WIDTH
      } else if (show) {
         if (window.width >= WIDTH + DETAILS_SCREEN_WIDTH)
            window.width
         else
            WIDTH + DETAILS_SCREEN_WIDTH
      } else WIDTH,
      height = window.height
   )
}

private fun calculate(context: DrawerContext, navigator: (TabType, Any?) -> Unit) {
   CalculationWindow(
      workstations = context.connectableElements,
      lines = context.lines,
      simulationContext = context.simulationContext
   ) { result -> navigator(TabType.RESULTS, result) }
}

@Composable
fun PanelScreen(
   modifier: Modifier = Modifier,
   context: DrawerContext,
   onRestart: () -> Unit,
   navigator: (TabType, Any?) -> Unit
) = Row(modifier) {
   Column(modifier = Modifier.weight(weight = 1f)) {
      DrawArea(
         modifier = Modifier.weight(1f),
         elementsState = context.elementsState,
         onPointerMove = context::onMouseMoved,
         onTap = { offset ->
            context.onCanvasTyped(offset) { onDetailsShow(show = context.selectedElementState.value != null) }
         }
      )
      ControlPanel(context, navigator)
   }

   if (context.selectedElementState.value != null && context.simulationContext.simulationPath == null) {
      DetailsScreen(
         modifier = Modifier
            .width(width = DETAILS_SCREEN_WIDTH.dp)
            .fillMaxHeight(),
         element = context.selectedElementState.value!!,
         deleter = context::removeElement,
         saver = context::changeElement
      )
   }
   if (context.simulationContext.simulationPath != null) {
      SimulationScreen(
         modifier = Modifier
            .width(width = DETAILS_SCREEN_WIDTH.dp)
            .background(Color.Green)
            .fillMaxHeight(),
         stopState = context.simulationContext.simulationStoppedState,
         simulationState = context.messageState,
         startState = context.simulationContext.simulationStartedState,
         path = context.simulationContext.simulationPath!!,
         onRestart = onRestart,
         onNext = context::next,
         onStop = context::stop,
         onResume = context::resume
      )
   }
}

@Composable
private fun ControlPanel(context: DrawerContext, navigator: (TabType, Any?) -> Unit) = Row(
   modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(align = Alignment.Bottom)
      .background(Color.White)
      .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
      .padding(10.dp)
) {
   Row(modifier = Modifier.weight(weight = 1f)) {
      Image(imageFromResource("workstation.png"),
         modifier = Modifier
            .width(32.dp)
            .height(32.dp)
            .clickable { context.selectedElementType = ElementType.WORKSTATION }
      )
      Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
      Image(imageFromResource("communication_node.png"),
         modifier = Modifier
            .width(32.dp)
            .height(32.dp)
            .clickable { context.selectedElementType = ElementType.COMMUNICATION_NODE }
      )
      Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
      Image(imageFromResource("line.jpg"),
         modifier = Modifier
            .width(32.dp)
            .height(32.dp)
            .clickable { context.selectedElementType = ElementType.LINE }
      )
      Spacer(modifier = Modifier.width(20.dp))
      Button(onClick = context::undo) { Text(text = "Undo") }
      Spacer(modifier = Modifier.width(20.dp))
      Button(onClick = { context.cancel(); onDetailsShow(false) }) { Text(text = "Cancel") }
      Spacer(modifier = Modifier.width(20.dp))
      Button(onClick = { calculate(context, navigator) }) { Text(text = "Calculate") }
   }
   Button(onClick = context::clear) { Text(text = "Clear") }
}

@Composable
private fun DrawArea(
   modifier: Modifier = Modifier,
   elementsState: MutableState<List<Element>>,
   onPointerMove: (Offset) -> Boolean,
   onTap: (Offset) -> Unit
) = Canvas(
   modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .pointerMoveFilter(onMove = onPointerMove, onEnter = { true })
      .tapGestureFilter { onTap(it) }
) {
   elementsState.value.forEach {
      it.onDraw(this)
   }
}
