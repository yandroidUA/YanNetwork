package com.github.yandroidua.ui.screens


import androidx.compose.desktop.AppManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.MAIN_WINDOW_TITLE
import com.github.yandroidua.ui.WIDTH
import com.github.yandroidua.ui.elements.*
import com.github.yandroidua.ui.utils.StartEndOffset

data class PageContext(
        val elementsState: MutableState<List<Element>>,
        val selectedElementState: MutableState<Element?>,
        var workstationCounter: Int = 0,
        var lineCreationLastTouchOffset: Offset? = null,
        var selectedElementType: ElementType? = null /* represent type clicked from bottom control panel bar */
)

private fun getElementOrNull(elements: List<Element>, offset: Offset): Element? {
    return elements.filterIsInstance<ImageControlElement>().find { it.isInOffset(offset) }
            ?: elements.find { it.isInOffset(offset) }
}

private fun onDetailsShow(show: Boolean) {
    val window = AppManager.windows.find { it.title == MAIN_WINDOW_TITLE } ?: return
    window.setSize(width = if (show) WIDTH + 200 else WIDTH, height = window.height)
}

@Composable
fun PanelScreen(modifier: Modifier = Modifier) = Row(modifier) {
    val pageContext = PageContext(
            elementsState = remember { mutableStateOf(emptyList()) },
            selectedElementState = remember { mutableStateOf(null) }
    )
    Column(modifier = Modifier.weight(weight = 1f)) {
        DrawArea(pageContext) { onDetailsShow(show = pageContext.selectedElementState.value != null) }
        ControlPanel(pageContext)
    }
    if (pageContext.selectedElementState.value != null) {
        DetailsScreen(
                modifier = Modifier
                        .width(width = 200.dp)
                        .background(Color.Green)
                        .fillMaxHeight(),
                element = pageContext.selectedElementState.value!!
        ) {
            //todo find and save this element
            pageContext.selectedElementState.value = it
        }
    }
}

@Composable
private fun ControlPanel(context: PageContext) = Row(
        modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Bottom)
                .background(Color.White)
                .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
                .padding(10.dp)
) {
    Image(asset = imageFromResource("workstation.png"),
            modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clickable { context.changeSelectedType(ElementType.WORKSTATION) }
    )
    Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
    Image(asset = imageFromResource("line.jpg"),
            modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clickable { context.changeSelectedType(ElementType.LINE) }
    )
    Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
    Button(onClick = context::undo) { Text(text = "Undo") }
    Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
    Button(onClick = context::onCancel) { Text(text = "Cancel") }
    Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
    Button(onClick = context::clear) { Text(text = "Clear") }
}

@Composable
private fun ColumnScope.DrawArea(
        pageContext: PageContext,
        onDetailInfoClicked: (Element) -> Unit
) = Canvas(
        modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .weight(1f)
                .pointerMoveFilter(onMove = { pageContext.onMouseMoved(it) }, onEnter =  { true })
                .tapGestureFilter { pageContext.onCanvasTyped(position = it, onDetailInfoClicked) }
) {
    pageContext.elementsState.value.forEach {
        it.onDraw(this)
    }
}

private fun PageContext.changeSelectedType(type: ElementType) {
    selectedElementType = type
}

private fun PageContext.onCanvasTyped(
        position: Offset,
        onDetailInfoClicked: (Element) -> Unit
) {
    selectedElementState.value = null
    onDetailsShow(false)
    val currentSelectedElementType = selectedElementType
    if (checkInfoClick(this, currentSelectedElementType, position, onDetailInfoClicked)) return //info displayed
    if (currentSelectedElementType == null) return  // wtf, nothing selected PANIC!
    when (currentSelectedElementType) {
        ElementType.WORKSTATION -> onWorkstationCreate(this, position)
        ElementType.LINE -> onLineCreate(this, position)
    }
}

private fun PageContext.onMouseMoved(position: Offset): Boolean {
    if (selectedElementType != ElementType.LINE) return false
    elementsState.value = elementsState.value.toMutableList().apply {
        val creatingLineIndex = indexOfFirst { it is Line && it.state == Line.State.CREATING }
        if (creatingLineIndex == -1) return false
        set(creatingLineIndex, (get(creatingLineIndex) as Line).copy(
                startEndOffset =  StartEndOffset(
                        startPoint =(get(creatingLineIndex) as Line).startEndOffset.startPoint,
                        endPoint = position
                ),
                isInMovement = true
        ))
    }
    return true
}

private fun PageContext.onCancel() {
    val indexOfActiveLine = elementsState.value.indexOfFirst { it is Line && it.state == Line.State.CREATING }
    if (indexOfActiveLine != -1) {
        elementsState.value = elementsState.value.toMutableList().apply { removeAt(indexOfActiveLine) }
    }
    selectedElementType = null
    lineCreationLastTouchOffset = null
    selectedElementState.value = null
    onDetailsShow(false)
}

private fun PageContext.clear() {
    selectedElementType = null
    workstationCounter = 0
    lineCreationLastTouchOffset = null
    selectedElementState.value = null
    elementsState.value = emptyList()
    onDetailsShow(false)
}

private fun PageContext.undo() {
    val lastElement = elementsState.value.lastOrNull()
    if (lastElement == null) {
        selectedElementType = null
        workstationCounter = 0
        lineCreationLastTouchOffset = null
        selectedElementState.value = null
        return
    }
    elementsState.value = when (lastElement.type) {
        ElementType.WORKSTATION -> {
            workstationCounter--
            elementsState.value.toMutableList().apply { removeLast() }
        }
        ElementType.LINE -> {
            lineCreationLastTouchOffset = null
            elementsState.value.toMutableList().apply { removeLast() }
        }
    }
}

private fun checkInfoClick(
        context: PageContext,
        type: ElementType?,
        position: Offset,
        onDetailInfoClicked: (Element) -> Unit
): Boolean {
    val elementOnPosition = getElementOrNull(context.elementsState.value, position) ?: return false
    return when (elementOnPosition.type) {
        ElementType.WORKSTATION -> if(type != ElementType.LINE) {
            context.selectedElementState.value = elementOnPosition
            onWorkstationInfo(elementOnPosition as Workstation, onDetailInfoClicked)
            true
        } else false
        ElementType.LINE -> {
            context.selectedElementState.value = elementOnPosition
            onLineInfo(elementOnPosition as Line, onDetailInfoClicked)
            true
        }
    }
}

private fun onLineInfo(line: Line, onDetailInfoClicked: (Element) -> Unit) {
    println("Line info")
    onDetailInfoClicked(line)
}

private fun onWorkstationInfo(workstation: Workstation, onDetailInfoClicked: (Element) -> Unit) {
    println("Workstation info")
    onDetailInfoClicked(workstation)
}

private fun onWorkstationCreate(context: PageContext, offset: Offset) {
    // reset line creation status
    context.lineCreationLastTouchOffset = null

    val item = getElementOrNull(context.elementsState.value, offset)
    if (item?.type == ElementType.WORKSTATION) return // cannot add workstation, because another workstation is near

    context.elementsState.value = context.elementsState.value.toMutableList().apply {
        add(Workstation(context.workstationCounter, offset))
    }
    context.workstationCounter++

}

private fun onLineCreate(context: PageContext, offset: Offset) {
    val typedElement = getElementOrNull(context.elementsState.value, offset) ?: return
    if (typedElement.type != ElementType.WORKSTATION) return
    if (context.lineCreationLastTouchOffset == null) {
        // this is first click, line must be created after second click on workstation
        context.lineCreationLastTouchOffset = typedElement.center
        context.elementsState.value = context.elementsState.value.toMutableList().apply {
            add(Line(
                    startEndOffset = StartEndOffset(
                            startPoint = typedElement.center,
                            endPoint = offset
                    ),
                    color = Color.Black,
                    state = Line.State.CREATING,
                    firstStationId = (typedElement as Workstation).id,
                    secondStationId = -1
            ))
        }
        return
    }
    // on second click of another workstation
    if (typedElement.isInOffset(context.lineCreationLastTouchOffset!!)) return
    context.elementsState.value = context.elementsState.value.toMutableList().apply {
        val creatingLineIndex = indexOfFirst { it is Line && it.state == Line.State.CREATING }
        if (creatingLineIndex == -1) return@apply
        set(creatingLineIndex, (get(creatingLineIndex) as Line).copy(
                state = Line.State.CREATED,
                startEndOffset =  StartEndOffset(
                        startPoint = context.lineCreationLastTouchOffset!!,
                        endPoint = typedElement.center
                ),
                secondStationId = (typedElement as Workstation).id,
                isInMovement = false
        ))
    }
    context.lineCreationLastTouchOffset = null //clear line creation state
}
