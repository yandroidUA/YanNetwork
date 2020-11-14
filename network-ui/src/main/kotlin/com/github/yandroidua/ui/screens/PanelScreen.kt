package com.github.yandroidua.ui.screens


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
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.*
import com.github.yandroidua.ui.utils.StartEndOffset

data class PageContext(
        val elementsState: MutableState<List<Element>>,
        var selectedElement: Element? = null,
        var lineCreationLastTouchOffset: Offset? = null,
        var selectedElementType: ElementType? = null /* represent type clicked from bottom control panel bar */
)

private fun getElementOrNull(elements: List<Element>, offset: Offset): Element? {
    return elements.filterIsInstance<ImageControlElement>().find { it.isInOffset(offset) }
            ?: elements.find { it.isInOffset(offset) }
}

@Composable
fun PanelScreen(modifier: Modifier = Modifier, onDetailInfoClicked: (Element) -> Unit) = Column {
    val pageContext = PageContext(
            elementsState = remember { mutableStateOf(emptyList()) }
    )
    DrawArea(pageContext, onDetailInfoClicked)
    ControlPanel(pageContext)
}

@Composable
private fun ControlPanel(context: PageContext) = Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(align = Alignment.Bottom)
        .padding(10.dp)
        .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
) {
    Image(
            asset = imageFromResource("workstation.png"),
            modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clickable { context.changeSelectedType(ElementType.WORKSTATION) }
    )
    Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
    Image(
            asset = imageFromResource("line.jpg"),
            modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clickable { context.changeSelectedType(ElementType.LINE) }
    )
    Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
    Button(onClick = {}) {
        Text(text = "Undo")
    }
}

@Composable
private fun ColumnScope.DrawArea(pageContext: PageContext, onDetailInfoClicked: (Element) -> Unit) = Canvas(modifier = Modifier
        .fillMaxSize()
        .background(Color.Red)
        .weight(1f)
        .pointerMoveFilter(onMove = {
            if (pageContext.selectedElementType != ElementType.LINE) return@pointerMoveFilter false
            pageContext.elementsState.value = pageContext.elementsState.value.toMutableList().apply {
                val creatingLineIndex = indexOfFirst { it is Line && it.state == Line.State.CREATING }
                if (creatingLineIndex == -1) return@pointerMoveFilter false
                set(creatingLineIndex, (get(creatingLineIndex) as Line).copy(
                        startEndOffset =  StartEndOffset(
                                startPoint =(get(creatingLineIndex) as Line).startEndOffset.startPoint,
                                endPoint = it
                        ),
                        isInMovement = true
                ))
            }
            println("Move $it")
            true
        }, onEnter =  { true })
        .tapGestureFilter { pageContext.onCanvasTyped(position = it, onDetailInfoClicked) }
) {
    pageContext.elementsState.value.forEach {
        it.onDraw(this)
    }
}

private fun PageContext.changeSelectedType(type: ElementType) {
    selectedElementType = type
}

private fun PageContext.onCanvasTyped(position: Offset, onDetailInfoClicked: (Element) -> Unit) {
    val currentSelectedElementType = selectedElementType ?: return // wtf, nothing selected PANIC!
    if (checkInfoClick(this, currentSelectedElementType, position, onDetailInfoClicked)) return //info displayed
    when (currentSelectedElementType) {
        ElementType.WORKSTATION -> onWorkstationCreate(this, position)
        ElementType.LINE -> onLineCreate(this, position)
    }
}

private fun checkInfoClick(context: PageContext, type: ElementType, position: Offset, onDetailInfoClicked: (Element) -> Unit): Boolean {
    val elementOnPosition = getElementOrNull(context.elementsState.value, position) ?: return false
    return when (elementOnPosition.type) {
        ElementType.WORKSTATION -> if(type != ElementType.LINE) {
            onWorkstationInfo(elementOnPosition as Workstation, onDetailInfoClicked)
            true
        } else false
        ElementType.LINE -> { onLineInfo(elementOnPosition as Line, onDetailInfoClicked); true }
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
        add(Workstation(offset))
    }

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
                    state = Line.State.CREATING
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
                isInMovement = false
        ))
    }
    context.lineCreationLastTouchOffset = null //clear line creation state
}
