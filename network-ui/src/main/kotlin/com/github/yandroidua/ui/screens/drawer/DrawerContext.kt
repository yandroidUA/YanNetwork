package com.github.yandroidua.ui.screens.drawer

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import com.github.yandroidua.simulation.Simulation
import com.github.yandroidua.simulation.buildConfiguration
import com.github.yandroidua.simulation.models.Event
import com.github.yandroidua.simulation.models.SimulationPath
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementMessage
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.mappers.mapToSimulation
import com.github.yandroidua.ui.mappers.mapToUiEvent
import com.github.yandroidua.ui.models.PathResultElements
import com.github.yandroidua.ui.models.SimulationResultModel
import com.github.yandroidua.ui.models.StartEndOffset
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DrawerContext(
    /**
     * Contains of elements that draws on Canvas
     */
    val elementsState: MutableState<List<Element>>,
    /**
     * Contains selected element
     *
     * used to select in Panel and when tap draw in to canvas also in details screens
     */
    val selectedElementState: MutableState<Element?>,
    /**
     * Contains simulation messages
     */
    val messageState: MutableState<SimulationResultModel?>,
    /**
     * Contains element's count, used for ID for new element
     */
    var elementCounter: Int = 0,
    /**
     * Contains information about message
     * fill during simulation
     */
    var messageContext: MessageContext? = null,
    /**
     * Contains last touch Offset value
     * used to display movement of new line
     */
    var lineCreationLastTouchOffset: Offset? = null,
    /**
     * Hold selected type
     * used to create new elements and to open details
     */
    var selectedElementType: ElementType? = null,
    /**
     * Contains info for simulation
     */
    val simulationContext: SimulationContext
) {

   val lines: List<ElementLine>
      get() = elementsState.value.filterIsInstance<ElementLine>()

   val connectableElements: List<ConnectableElement>
      get() = elementsState.value.filterIsInstance<ConnectableElement>()

   val workstationElements: List<ElementWorkstation>
      get() = elementsState.value.filterIsInstance<ElementWorkstation>()

   val communicationNodeElements: List<ElementCommunicationNode>
      get() = elementsState.value.filterIsInstance<ElementCommunicationNode>()

   fun startSimulation() {
      simulationContext.simulationJob?.cancel()
      deleteMessage()
      if (simulationContext.simulationPath == null) return
      simulationContext.simulationStartedState.value = true
      simulationContext.simulationJob = GlobalScope.launch {
         val simulation = configureSimulation()
         simulation.simulate()
             .drop((Simulation.INFO_EMITS_PER_SEND + 1) * simulationContext.simulationStartStep)
             .map { it.mapToUiEvent() }
             .flowOn(Dispatchers.Default)
             .flatMapConcat { event -> handleEvent(event) }
             .collect { event ->
                onMessageChanged(event)
                messageState.value = event
                if (event is SimulationResultModel.EndSimulation) {
                   simulationContext.simulationStartedState.value = false
                }
             }
      }
   }

   fun undo() {
      selectedElementType = null
      lineCreationLastTouchOffset = null
      selectedElementState.value = null
      removeElement(elementsState.value.lastOrNull() ?: return)
   }

   fun cancel() {
      selectedElementType = null
      lineCreationLastTouchOffset = null
      selectedElementState.value = null
      val indexOfActiveLine = elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
      if (indexOfActiveLine != -1) {
         elementsState.value = elementsState.value.toMutableList().apply { removeAt(indexOfActiveLine) }
      }
   }

   fun removeElement(element: Element) {
      when (element.type) {
         ElementType.WORKSTATION -> removeWorkstation(element as ElementWorkstation)
         ElementType.COMMUNICATION_NODE -> removeCommunicationNode(element as ElementCommunicationNode)
         ElementType.LINE -> removeLine(element as ElementLine)
         ElementType.MESSAGE -> removeMessage(element as ElementMessage)
      }
   }

   fun clear(onCleared: () -> Unit = {}) {
      selectedElementType = null
      elementCounter = 0
      lineCreationLastTouchOffset = null
      selectedElementState.value = null
      elementsState.value = emptyList()
      onCleared()
   }

   fun changeElement(element: Element) {
      // find position of this element, comparing by ID, because content and reference could be different
      val indexOfElement = elementsState.value.indexOfFirst { it.id == element.id }
      if (indexOfElement == -1) return
      elementsState.value = elementsState.value.toMutableList().apply {
         set(indexOfElement, element)
      }
   }

   fun onMouseMoved(offset: Offset): Boolean {
      // event need to be handled only for LINE
      if (selectedElementType != ElementType.LINE) return false
      val creatingLineIndex = elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
      if (creatingLineIndex == -1) return false
      val line = (elementsState.value.getOrNull(creatingLineIndex) as? ElementLine) ?: return false
      changeElement(
          element = line.copy(startEndOffset = line.startEndOffset.copy(endPoint = offset), isInMovement = true)
      )
      return true
   }

   fun onCanvasTyped(position: Offset, onElementDetected: (Element) -> Unit) {
      selectedElementState.value = null
      // check if clicked on item
      if (onTypedOnElement(position, onElementDetected)) return
      val currentSelectedElementType = selectedElementType ?: return
      when (currentSelectedElementType) {
         ElementType.WORKSTATION -> onWorkstationCreate(position)
         ElementType.COMMUNICATION_NODE -> onCommunicationNodeCreate(position)
         ElementType.LINE -> onLineCreate(position)
         ElementType.MESSAGE -> {}
      }
   }

   fun onMessageChanged(event: SimulationResultModel) {
      when (event) {
         is SimulationResultModel.TextSimulationModel -> deleteMessage()
         is SimulationResultModel.MessageStartModel -> createNewMessage(event)
         is SimulationResultModel.MessageMoveModel -> moveMessage(event)
         is SimulationResultModel.ErrorMessageModel -> deleteMessage()
         SimulationResultModel.EndSimulation -> deleteMessage()
      }
   }

   private fun handleEvent(model: SimulationResultModel): Flow<SimulationResultModel> {
      return when (model) {
         is SimulationResultModel.MessageStartModel -> divideAndEmitSendingEvent(model)
         else -> flowOf(model)
      }
   }

   private fun divideAndEmitSendingEvent(
       model: SimulationResultModel.MessageStartModel
   ): Flow<SimulationResultModel> = flow {
      messageContext = MessageContext(
          id = elementCounter.also { elementCounter++ },
          toId = model.to,
          lineId = model.by,
          fromId = model.from
      )
      emit(model)
      val fromWorkstation = (elementsState.value.find { it.id == model.from })?.center ?: return@flow
      val toWorkstation = (elementsState.value.find { it.id == model.to })?.center ?: return@flow
      repeat(model.time.toInt()) {
         delay(1)
         emit(SimulationResultModel.MessageMoveModel(
             model.from,
             model.to,
             model.by,
             lerp(fromWorkstation, toWorkstation, it / model.time.toFloat()),
             model.time
         ))
      }
   }

   private fun configureSimulation(): Simulation {
      return Simulation(
          configuration = buildConfiguration {
             path = simulationContext.simulationPath!!.mapToSimulation()
             infoPacketSize = simulationContext.infoPacketSize
             sysPacketSize = simulationContext.sysPacketSize
             size = simulationContext.size
          },
          models = elementsState.value.mapNotNull { it.mapToSimulation() }
      )
   }

   private fun createNewMessage(event: SimulationResultModel.MessageStartModel) {
      val fromWorkstation = elementsState.value.find { it.id == event.from } ?: return
      elementsState.value = elementsState.value.toMutableList().apply {
         add(ElementMessage(messageContext!!.id, fromWorkstation.center))
      }
   }

   private fun moveMessage(event: SimulationResultModel.MessageMoveModel) {
      val msgContext = messageContext ?: return
      val index = elementsState.value.indexOfFirst { it.type == ElementType.MESSAGE }
      if (index == -1) return
      elementsState.value = elementsState.value.toMutableList().apply {
         set(index, ElementMessage(msgContext.id, event.offset))
      }
   }

   private fun deleteMessage() {
      messageContext = null
      val messageElement = elementsState.value.find { it.type == ElementType.MESSAGE }
      messageElement?.let {
         elementsState.value = elementsState.value.toMutableList().apply { remove(messageElement) }
      }
   }

   private fun onCommunicationNodeCreate(offset: Offset) {
      lineCreationLastTouchOffset = null
      val clickedItem = findElementOrNull(offset)
      // cannot add element near another connectable element
      if (clickedItem?.connectable == true) return
      addElement(ElementCommunicationNode(elementCounter, offset))
   }

   private fun onWorkstationCreate(offset: Offset) {
      lineCreationLastTouchOffset = null
      val clickedItem = findElementOrNull(offset)
      // cannot add element near another connectable element
      if (clickedItem?.connectable == true) return
      addElement(ElementWorkstation(elementCounter, offset))
   }

   private fun onLineCreate(offset: Offset) {
      val clickedItem = findElementOrNull(offset)
      if (clickedItem !is ConnectableElement) return
      if (lineCreationLastTouchOffset == null) {
         createNewLine(clickedItem, offset)
         return
      } else {
         // to prevent creating cycle workstation connection
         if (clickedItem.isInOffset(lineCreationLastTouchOffset!!)) return
         endLineCreation(clickedItem, offset)
      }
   }

   private fun createNewLine(connectableElement: ConnectableElement, offset: Offset) {
      lineCreationLastTouchOffset = connectableElement.center
      addElement(element = ElementLine(
          id = elementCounter.also { elementCounter++ },
          startEndOffset = StartEndOffset(
              startPoint = connectableElement.center,
              endPoint = offset
          ),
          color = Color.Black,
          state = ElementLine.State.CREATING,
          firstStationId = connectableElement.id,
          secondStationId = -1
      ))
   }

   private fun endLineCreation(connectableElement: ConnectableElement, offset: Offset) {
      val creatingLineIndex = elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
      if (creatingLineIndex == -1) return
      elementsState.value = elementsState.value.toMutableList().apply {
         val newLine = (get(creatingLineIndex) as ElementLine).copy(
             state = ElementLine.State.CREATED,
             startEndOffset =  StartEndOffset(
                 startPoint = lineCreationLastTouchOffset!!,
                 endPoint = connectableElement.center
             ),
             secondStationId = connectableElement.id,
             isInMovement = false
         )
         set(creatingLineIndex, newLine)
         (elementsState.value.find { it.id == newLine.firstStationId } as? ConnectableElement)?.lineIds?.add(newLine.id)
         (elementsState.value.find { it.id == newLine.secondStationId } as? ConnectableElement)?.lineIds?.add(newLine.id)
      }
      lineCreationLastTouchOffset = null
   }

   private fun addElement(element: Element) {
      elementsState.value = elementsState.value.toMutableList().apply { add(element) }
      elementCounter++
   }

   private fun findElementOrNull(click: Offset): Element? {
      // firstly search in ImageControlElements (workstations, communicationNodes, messages)
      return elementsState.value.filterIsInstance<ConnectableElement>().find { it.isInOffset(click) }
          // then search in all elements
          ?: elementsState.value.find { it.isInOffset(click) }
   }

   private fun onTypedOnElement(position: Offset, onElementDetected: (Element) -> Unit): Boolean {
      val clickedElement = findElementOrNull(position) ?: return false
      return when (clickedElement.type) {
         ElementType.WORKSTATION, ElementType.COMMUNICATION_NODE -> {
            // if clicked on workstation and current selected element is LINE it can be
            // process of linking stations so need get chance to handle this event to others
            if (selectedElementType == ElementType.LINE) return false
            selectedElementState.value = clickedElement
            onElementDetected(clickedElement)
            true
         }
         ElementType.LINE -> {
            selectedElementState.value = clickedElement
            onElementDetected(clickedElement)
            true
         }
         ElementType.MESSAGE -> false
      }
   }

   private fun removeWorkstation(elementWorkstation: ElementWorkstation) {
      removeConnectableElement(elementWorkstation)
   }

   private fun removeCommunicationNode(communicationNode: ElementCommunicationNode) {
     removeConnectableElement(communicationNode)
   }

   private fun removeConnectableElement(connectableElement: ConnectableElement) {
      // searching connected lines to this workstation
      elementsState.value = elementsState.value.toMutableList().apply { remove(connectableElement) }
      connectableElement.lineIds.forEach { lineId ->
         (elementsState.value.find { it.id == lineId } as? ElementLine)?.let(this::removeLine)
      }
   }

   private fun removeLine(elementLine: ElementLine) {
      // searching connectable elements that connected to this line
      (elementsState.value.find { it.id == elementLine.firstStationId } as? ConnectableElement)?.lineIds?.remove(elementLine.id)
      (elementsState.value.find { it.id == elementLine.secondStationId } as? ConnectableElement)?.lineIds?.remove(elementLine.id)
      elementsState.value = elementsState.value.toMutableList().apply { remove(elementLine) }
   }

   private fun removeMessage(elementMessage: ElementMessage) {
      elementsState.value = elementsState.value.toMutableList().apply { remove(elementMessage) }
   }

}