package com.github.yandroidua.ui.screens.drawer


import androidx.compose.desktop.AppManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.Simulation
import com.github.yandroidua.simulation.buildConfiguration
import com.github.yandroidua.ui.MAIN_WINDOW_TITLE
import com.github.yandroidua.ui.WIDTH
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.ElementMessage
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.elements.base.ImageControlElement
import com.github.yandroidua.ui.mappers.mapToSimulation
import com.github.yandroidua.ui.mappers.mapToUiEvent
import com.github.yandroidua.ui.models.SimulationResultModel
import com.github.yandroidua.ui.screens.details.DetailsScreen
import com.github.yandroidua.ui.models.PathResultElements
import com.github.yandroidua.ui.models.StartEndOffset
import com.github.yandroidua.ui.models.TabType
import com.github.yandroidua.ui.screens.CalculationWindow
import com.github.yandroidua.ui.screens.SimulationScreen
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// ------------------------------------Constants------------------------------------------------------------------------

private const val DETAILS_SCREEN_WIDTH = 350

// ------------------------------------PanelPageContext-----------------------------------------------------------------



data class PanelPageContext(
        val elementsState: MutableState<List<Element>>,
        val selectedElementState: MutableState<Element?>,
        val reloadState: MutableState<Boolean>,
        val messageState: MutableState<SimulationResultModel?>,
        var dropValue: Int = 0,
        var elementCounter: Int = 0,
        var messageContext: MessageContext? = null,
        var lineCreationLastTouchOffset: Offset? = null,
        var selectedElementType: ElementType? = null, /* represent type clicked from bottom control panel bar */
        var pathToSimulate: PathResultElements? = null,
        var simulationJob: Job? = null
)

private fun PanelPageContext.changeSelectedType(type: ElementType) {
    selectedElementType = type
}

//@Composable
fun PanelPageContext.launchSimulation() {
    if (pathToSimulate == null) return
    deleteMessage()
    simulationJob?.cancel()
    simulationJob = GlobalScope.launch {
        val simulation = Simulation(
                configuration = buildConfiguration {
                    path = pathToSimulate!!.mapToSimulation()
                    infoPacketSize = 256
                    sysPacketSize = 10
                    size = 65536
                },
                models = elementsState.value.map { it.mapToSimulation() }.filterNotNull()
        )
        simulation.simulate()
                .drop((Simulation.INFO_EMITS_PER_SEND + 1) * dropValue)
                .map { it.mapToUiEvent() }
                .flowOn(Dispatchers.Default)
                .flatMapConcat { model ->
                    when (model) {
                        is SimulationResultModel.MessageStartModel -> flow {
                            println("Sending")
                            val id = elementCounter
                            messageContext = MessageContext(
                                    id = id,
                                    toId = model.to,
                                    lineId = model.by,
                                    fromId = model.from
                            )
                            elementCounter++
                            emit(model)
                            val fromWorkstation = (elementsState.value.findLast { it.id == model.from })?.center ?: return@flow
                            val toWorkstation = (elementsState.value.findLast { it.id == model.to })?.center ?: return@flow
                            repeat(model.time.toInt()) {
                                delay(1)
                                val event = SimulationResultModel.MessageMoveModel(
                                        model.from,
                                        model.to,
                                        model.by,
                                        lerp(fromWorkstation, toWorkstation, it / model.time.toFloat()),
                                        model.time
                                )
                                println("Fraction: ${it/model.time.toFloat()}")
                                emit(event)
                            }
                        }
                        else -> flowOf(model)
                    }
                }.collect {
                    println("Collect: $it")
                    onMessageChanged(it)
                    messageState.value = it
                }
    }
}

fun PanelPageContext.onMessageChanged(event: SimulationResultModel) {
    when (event) {
        is SimulationResultModel.TextSimulationModel -> deleteMessage()
        is SimulationResultModel.MessageStartModel -> createNewMessage(event)
        is SimulationResultModel.MessageMoveModel -> moveMessage(event)
        is SimulationResultModel.ErrorMessageModel -> deleteMessage()
    }
}

private fun PanelPageContext.createNewMessage(event: SimulationResultModel.MessageStartModel) {
    val fromWorkstation = elementsState.value.find { it.id == event.from } ?: return
    // adding new Message to state
    println("createNewMessage $event")
    elementsState.value = elementsState.value.toMutableList().apply {
        add(ElementMessage(messageContext!!.id, fromWorkstation.center))
    }
}

private fun PanelPageContext.moveMessage(event: SimulationResultModel.MessageMoveModel) {
    val msgContext = messageContext ?: return
    val index = elementsState.value.indexOfFirst { it.type == ElementType.MESSAGE }
    if (index == -1) {
        println("not found")
        return
    }
    println("moveMessage $event")
    elementsState.value = elementsState.value.toMutableList().apply {
        set(index, ElementMessage(msgContext.id, event.offset))
    }
}

private fun PanelPageContext.deleteMessage() {
    messageContext = null
    val el = elementsState.value.find { it.type == ElementType.MESSAGE }
    el?.let {
        elementsState.value = elementsState.value.toMutableList().apply { remove(el) }
    }
}

private fun PanelPageContext.onCanvasTyped(
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
        ElementType.COMMUNICATION_NODE -> onCommunicationNodeCreate(this, position)
    }
}

private fun PanelPageContext.onMouseMoved(position: Offset): Boolean {
//    if (selectedElementType != ElementType.LINE) return false
    elementsState.value = elementsState.value.toMutableList().apply {
        val creatingLineIndex = indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
        if (creatingLineIndex == -1) return false
        set(creatingLineIndex, (get(creatingLineIndex) as ElementLine).copy(
                startEndOffset =  StartEndOffset(
                        startPoint =(get(creatingLineIndex) as ElementLine).startEndOffset.startPoint,
                        endPoint = position
                ),
                isInMovement = true
        ))
    }
    return true
}

private fun PanelPageContext.changeElement(newElement: Element) {
    val elementIndex = elementsState.value.indexOfFirst { it.id == newElement.id }
    if (elementIndex == -1) return
    elementsState.value = elementsState.value.toMutableList().apply {
        set(elementIndex, newElement)
    }
}

private fun PanelPageContext.removeElement(element: Element) {
    elementsState.value = when (element.type) {
        ElementType.WORKSTATION -> {
            val connectedLines = elementsState.value.filterIsInstance<ElementLine>()
                    .filter { it.firstStationId == element.id || it.secondStationId == element.id }
            connectedLines.forEach { removeElement(it) }
            elementsState.value.toMutableList().apply { remove(element) }
        }
        ElementType.LINE -> {
            lineCreationLastTouchOffset = null
            val line = element as ElementLine
            (elementsState.value.find { it.id == line.secondStationId } as? ConnectableElement)?.lineIds?.remove(line.id)
            (elementsState.value.find { it.id == line.firstStationId } as? ConnectableElement)?.lineIds?.remove(line.id)
            elementsState.value.toMutableList().apply { remove(element) }
        }
        ElementType.COMMUNICATION_NODE -> {
            val connectedLines = elementsState.value.filterIsInstance<ElementLine>()
                    .filter { it.firstStationId == element.id || it.secondStationId == element.id }
            connectedLines.forEach { removeElement(it) }
            elementsState.value.toMutableList().apply { remove(element) }
        }
        ElementType.MESSAGE -> {
            elementsState.value.toMutableList().apply { remove(element) }
        }
    }
    elementCounter--
    onCancel()
}

private fun PanelPageContext.calculate(navigator: (TabType, Any?) -> Unit) {
    val workstations = elementsState.value.filterIsInstance<ConnectableElement>()
    val lines = elementsState.value.filterIsInstance<ElementLine>()
    CalculationWindow(workstations = workstations, lines) { result -> navigator(TabType.RESULTS, result) }
}

private fun PanelPageContext.onCancel() {
    val indexOfActiveLine = elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
    if (indexOfActiveLine != -1) {
        elementsState.value = elementsState.value.toMutableList().apply { removeAt(indexOfActiveLine) }
    }
    selectedElementType = null
    lineCreationLastTouchOffset = null
    selectedElementState.value = null
    onDetailsShow(false)
}

private fun PanelPageContext.clear() {
    selectedElementType = null
    elementCounter = 0
    lineCreationLastTouchOffset = null
    selectedElementState.value = null
    elementsState.value = emptyList()
    onDetailsShow(false)
}

private fun PanelPageContext.undo() {
    val lastElement = elementsState.value.lastOrNull()
    if (lastElement == null) {
        selectedElementType = null
        elementCounter = 0
        lineCreationLastTouchOffset = null
        selectedElementState.value = null
        return
    }
    removeElement(lastElement)
}

// -----------------------------UtilsFunctions--------------------------------------------------------------------------

private fun onLineInfo(elementLine: ElementLine, onDetailInfoClicked: (Element) -> Unit) {
    println("Line info")
    onDetailInfoClicked(elementLine)
}

private fun onWorkstationInfo(elementWorkstation: ElementWorkstation, onDetailInfoClicked: (Element) -> Unit) {
    println("Workstation info")
    onDetailInfoClicked(elementWorkstation)
}

private fun onCommunicationNodeInfo(
        elementCommunicationNode: ElementCommunicationNode,
        onDetailInfoClicked: (Element) -> Unit
) {
    println("onCommunicationNodeInfo")
    onDetailInfoClicked(elementCommunicationNode)
}

private fun onWorkstationCreate(contextPanel: PanelPageContext, offset: Offset) {
    // reset line creation status
    contextPanel.lineCreationLastTouchOffset = null

    val item = getElementOrNull(contextPanel.elementsState.value, offset)
    if (item?.type == ElementType.WORKSTATION || item?.type == ElementType.COMMUNICATION_NODE) return // cannot add workstation, because another workstation is near

    contextPanel.elementsState.value = contextPanel.elementsState.value.toMutableList().apply {
        add(ElementWorkstation(contextPanel.elementCounter, offset))
    }
    contextPanel.elementCounter++

}

private fun onLineCreate(contextPanel: PanelPageContext, offset: Offset) {
    val typedElement = getElementOrNull(contextPanel.elementsState.value, offset) ?: return
    if (!typedElement.connectable) return
    if (contextPanel.lineCreationLastTouchOffset == null) {
        // this is first click, line must be created after second click on workstation
        contextPanel.lineCreationLastTouchOffset = typedElement.center
        contextPanel.elementsState.value = contextPanel.elementsState.value.toMutableList().apply {
            val newLine = ElementLine(
                    id = contextPanel.elementCounter.also { contextPanel.elementCounter++ },
                    startEndOffset = StartEndOffset(
                            startPoint = typedElement.center,
                            endPoint = offset
                    ),
                    color = Color.Black,
                    state = ElementLine.State.CREATING,
                    firstStationId = typedElement.id,
                    secondStationId = -1
            )
            add(newLine)
        }
        return
    }
    // on second click of another workstation
    if (typedElement.isInOffset(contextPanel.lineCreationLastTouchOffset!!)) return
    contextPanel.elementsState.value = contextPanel.elementsState.value.toMutableList().apply {
        val creatingLineIndex = indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
        if (creatingLineIndex == -1) return@apply
        val newLine = (get(creatingLineIndex) as ElementLine).copy(
                state = ElementLine.State.CREATED,
                startEndOffset =  StartEndOffset(
                        startPoint = contextPanel.lineCreationLastTouchOffset!!,
                        endPoint = typedElement.center
                ),
                secondStationId = typedElement.id,
                isInMovement = false
        )
        set(creatingLineIndex, newLine)
        (contextPanel.elementsState.value.find { it.id == newLine.firstStationId } as? ConnectableElement)?.lineIds?.add(newLine.id)
        (contextPanel.elementsState.value.find { it.id == newLine.secondStationId } as? ConnectableElement)?.lineIds?.add(newLine.id)
    }
    contextPanel.lineCreationLastTouchOffset = null //clear line creation state
}

private fun onCommunicationNodeCreate(contextPanel: PanelPageContext, offset: Offset) {
    // reset line creation status
    contextPanel.lineCreationLastTouchOffset = null

    val item = getElementOrNull(contextPanel.elementsState.value, offset)
    // cannot add workstation, because another workstation is near
    if (item?.type == ElementType.WORKSTATION || item?.type == ElementType.COMMUNICATION_NODE) return

    contextPanel.elementsState.value = contextPanel.elementsState.value.toMutableList().apply {
        add(ElementCommunicationNode(contextPanel.elementCounter, offset))
    }
    contextPanel.elementCounter++
}

private fun getElementOrNull(elements: List<Element>, offset: Offset): Element? {
    return elements.filterIsInstance<ImageControlElement>().find { it.isInOffset(offset) }
            ?: elements.find { it.isInOffset(offset) }
}

private fun onDetailsShow(show: Boolean) {
    val window = AppManager.windows.find { it.title == MAIN_WINDOW_TITLE } ?: return
    window.setSize(
            width = if (show && window.width <= WIDTH)
                WIDTH + DETAILS_SCREEN_WIDTH
            else
                if (window.width <= WIDTH + DETAILS_SCREEN_WIDTH)
                    WIDTH
                else
                    window.width,
            height = window.height
    )
}

private fun checkInfoClick(
        contextPanel: PanelPageContext,
        type: ElementType?,
        position: Offset,
        onDetailInfoClicked: (Element) -> Unit
): Boolean {
    val elementOnPosition = getElementOrNull(contextPanel.elementsState.value, position) ?: return false
    return when (elementOnPosition.type) {
        ElementType.WORKSTATION -> if(type != ElementType.LINE) {
            contextPanel.selectedElementState.value = elementOnPosition
            onWorkstationInfo(elementOnPosition as ElementWorkstation, onDetailInfoClicked)
            true
        } else false
        ElementType.LINE -> {
            contextPanel.selectedElementState.value = elementOnPosition
            onLineInfo(elementOnPosition as ElementLine, onDetailInfoClicked)
            true
        }
        ElementType.COMMUNICATION_NODE -> if(type != ElementType.LINE) {
            contextPanel.selectedElementState.value = elementOnPosition
            onCommunicationNodeInfo(elementOnPosition as ElementCommunicationNode, onDetailInfoClicked)
            true
        } else false
        ElementType.MESSAGE -> false //todo message info
    }
}

//---------------------------------UI-----------------------------------------------------------------------------------
//pageContext.messageFlow!!.collectAsState(initial = SimulationResultModel.TextSimulationModel(""), Dispatchers.IO + Job()),
@Composable
fun PanelScreen(
        modifier: Modifier = Modifier,
        pageContext: PanelPageContext,
        onRestart: () -> Unit,
        navigator: (TabType, Any?) -> Unit
) = Row(modifier) {
    Column(modifier = Modifier.weight(weight = 1f)) {
        DrawArea(Modifier.weight(1f), pageContext.elementsState, {pageContext.onMouseMoved(it)}) {
            pageContext.onCanvasTyped(it) {
                onDetailsShow(show = pageContext.selectedElementState.value != null)
            }
        }
        ControlPanel(pageContext, navigator)
    }

    if (pageContext.selectedElementState.value != null && pageContext.pathToSimulate == null) {
        DetailsScreen(
                modifier = Modifier
                        .width(width = DETAILS_SCREEN_WIDTH.dp)
                        .fillMaxHeight(),
                element = pageContext.selectedElementState.value!!,
                deleter = pageContext::removeElement,
                saver = pageContext::changeElement
        )
    }
    if (pageContext.pathToSimulate != null) {
        SimulationScreen(
                modifier = Modifier
                        .width(width = DETAILS_SCREEN_WIDTH.dp)
                        .background(Color.Green)
                        .fillMaxHeight(),
                simulationState = pageContext.messageState,
                path = pageContext.pathToSimulate!!,
                onRestart = {
                    pageContext.dropValue = 0
                    onRestart()
                },
                onStep = { step ->
                    pageContext.dropValue = step
                    onRestart()
                },
                updater = {}
        )
    }
}

@Composable
private fun ControlPanel(contextPanel: PanelPageContext, navigator: (TabType, Any?) -> Unit) = Row(
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
                        .clickable { contextPanel.changeSelectedType(ElementType.WORKSTATION) }
        )
        Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
        Image(imageFromResource("communication_node.png"),
                modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                        .clickable { contextPanel.changeSelectedType(ElementType.COMMUNICATION_NODE) }
        )
        Spacer(modifier = Modifier.wrapContentHeight().width(20.dp))
        Image(imageFromResource("line.jpg"),
                modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                        .clickable { contextPanel.changeSelectedType(ElementType.LINE) }
        )
        Spacer(modifier = Modifier.width(20.dp))
        Button(onClick = contextPanel::undo) { Text(text = "Undo") }
        Spacer(modifier = Modifier.width(20.dp))
        Button(onClick = contextPanel::onCancel) { Text(text = "Cancel") }
        Spacer(modifier = Modifier.width(20.dp))
        Button(onClick = { contextPanel.calculate(navigator) }) { Text(text = "Calculate") }
    }
    Button(onClick = contextPanel::clear) { Text(text = "Clear") }
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
                .pointerMoveFilter(onMove = onPointerMove, onEnter =  { true })
                .tapGestureFilter { onTap(it) }
) {
    elementsState.value.forEach {
        it.onDraw(this)
    }
}
