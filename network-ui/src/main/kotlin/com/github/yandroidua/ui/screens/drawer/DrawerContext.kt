package com.github.yandroidua.ui.screens.drawer

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import com.github.yandroidua.algorithm.BellmanFordAlgorithm
import com.github.yandroidua.simulation.RoutingTable
import com.github.yandroidua.simulation.Simulation
import com.github.yandroidua.simulation.buildConfiguration
import com.github.yandroidua.simulation.models.LineType
import com.github.yandroidua.simulation.models.SimulationRoutingTableEntry
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementMessage
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.mappers.mapToAlgorithmEntity
import com.github.yandroidua.ui.mappers.mapToSimulation
import com.github.yandroidua.ui.mappers.mapToUiEvent
import com.github.yandroidua.ui.models.SimulationResultModel
import com.github.yandroidua.ui.models.StartEndOffset
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.random.Random

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

   companion object {
      private val LINE_WEIGHTS = arrayOf(2, 3, 6, 7, 9, 10, 12, 16, 18, 21, 22, 30, 32)
   }

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
      simulationContext.simulationStoppedState.value = false
      simulationContext.next = false
      val events = configureSimulation().simulate().map { it.mapToUiEvent() }
      simulationContext.simulationJob = GlobalScope.launch {
         val startTime = System.currentTimeMillis()
         for (event in events) {
            messageState.value = event
            when (event) {
               is SimulationResultModel.MessageStartModel -> {
                  handleSendEvent(event)
                  checkStopAndNext()
                  deleteMessage()
               }
               SimulationResultModel.EndSimulation -> {
                  simulationContext.simulationStartedState.value = false
               }
               else -> onMessageChanged(event)
            }
         }
         val endTime = System.currentTimeMillis()
         println("Time: ${endTime - startTime}")
      }
   }

   fun stop() {
      simulationContext.simulationStoppedState.value = true
   }

   fun next() {
      simulationContext.next = true
      simulationContext.simulationStoppedState.value = false
   }

   fun resume() {
      simulationContext.next = false
      simulationContext.simulationStoppedState.value = false
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
      val indexOfActiveLine =
         elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
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
      val creatingLineIndex =
         elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
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
         ElementType.MESSAGE -> {
         }
      }
   }

   fun findConnections(element: Element): List<SimulationRoutingTableEntry> {
      return when (element.type) {
         ElementType.WORKSTATION -> findConnectableElementConnections(element as ConnectableElement)
         ElementType.COMMUNICATION_NODE -> findConnectableElementConnections(element as ConnectableElement)
         ElementType.LINE -> emptyList()
         ElementType.MESSAGE -> emptyList()
      }
   }

   private fun findConnectableElementConnections(connectableElement: ConnectableElement): List<SimulationRoutingTableEntry> {
      val alg = BellmanFordAlgorithm(
         workstations = connectableElements.map { it.mapToAlgorithmEntity() },
         lines = lines.map { it.mapToAlgorithmEntity() }
      )
      val routingTable = RoutingTable(
         allElements = elementsState.value.map { it.mapToSimulation() }.filterNotNull(),
         workstation = connectableElement.mapToSimulation()
      )
      return routingTable.routingTable { from, to ->
         val fromWorkstation = connectableElements.find { it.id == from } ?: return@routingTable null
         val toWorkstation = connectableElements.find { it.id == to } ?: return@routingTable null
         alg.calculate(from = fromWorkstation.mapToAlgorithmEntity(), to = toWorkstation.mapToAlgorithmEntity())
            .minByOrNull { it.summary }
            ?.mapToSimulation()
      }
   }

   private suspend fun checkStoppingFlag() = suspendCancellableCoroutine<Unit> {
      while (simulationContext.simulationStoppedState.value) {}
      it.resume(Unit)
   }

   private suspend fun checkStopAndNext() {
      if (simulationContext.next) {
         stop()
      }
      checkStoppingFlag()
   }

   private fun onMessageChanged(event: SimulationResultModel) {
      when (event) {
         is SimulationResultModel.TextSimulationModel -> deleteMessage()
         is SimulationResultModel.MessageStartModel -> createNewMessage(event)
         is SimulationResultModel.MessageMoveModel -> moveMessage(event)
         is SimulationResultModel.ErrorMessageModel -> deleteMessage()
         SimulationResultModel.EndSimulation -> deleteMessage()
      }
   }

   private suspend fun handleSendEvent(model: SimulationResultModel.MessageStartModel) {
      messageContext = MessageContext(
         id = elementCounter.also { elementCounter++ },
         toId = model.to,
         lineId = model.by,
         fromId = model.from
      )
      onMessageChanged(model)
      val fromWorkstation = (elementsState.value.find { it.id == model.from })?.center ?: return
      val toWorkstation = (elementsState.value.find { it.id == model.to })?.center ?: return
      repeat(model.time.toInt()) {
         delay(1)
         onMessageChanged(
            SimulationResultModel.MessageMoveModel(
               model.from,
               model.to,
               model.by,
               lerp(fromWorkstation, toWorkstation, it / model.time.toFloat()),
               model.time
            )
         )
         checkStoppingFlag()
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
         endLineCreation(clickedItem)
      }
   }

   private fun getRandomLineWeight(): Int {
      val random = Random(System.currentTimeMillis())
      return LINE_WEIGHTS[random.nextInt(0, LINE_WEIGHTS.size)]
   }

   private fun createNewLine(connectableElement: ConnectableElement, offset: Offset) {
      lineCreationLastTouchOffset = connectableElement.center
      addElement(
         element = ElementLine(
            id = elementCounter.also { elementCounter++ },
            startEndOffset = StartEndOffset(
               startPoint = connectableElement.center,
               endPoint = offset
            ),
            weight = getRandomLineWeight(),
            color = Color.Black,
            state = ElementLine.State.CREATING,
            firstStationId = connectableElement.id,
            secondStationId = -1,
            errorChance = 0f,
            lineType = LineType.DUPLEX
         )
      )
   }

   private fun endLineCreation(connectableElement: ConnectableElement) {
      val creatingLineIndex =
         elementsState.value.indexOfFirst { it is ElementLine && it.state == ElementLine.State.CREATING }
      if (creatingLineIndex == -1) return
      val line = elementsState.value[creatingLineIndex] as ElementLine
//      if (line.firstStationId == connectableElement.id) return
      elementsState.value = elementsState.value.toMutableList().apply {
         val newLine = line.copy(
            state = ElementLine.State.CREATED,
            startEndOffset = StartEndOffset(
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
      (elementsState.value.find { it.id == elementLine.firstStationId } as? ConnectableElement)?.lineIds?.remove(
         elementLine.id
      )
      (elementsState.value.find { it.id == elementLine.secondStationId } as? ConnectableElement)?.lineIds?.remove(
         elementLine.id
      )
      elementsState.value = elementsState.value.toMutableList().apply { remove(elementLine) }
   }

   private fun removeMessage(elementMessage: ElementMessage) {
      elementsState.value = elementsState.value.toMutableList().apply { remove(elementMessage) }
   }

}