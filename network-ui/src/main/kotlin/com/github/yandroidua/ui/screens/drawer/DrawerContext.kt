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
import com.github.yandroidua.simulation.models.packets.PacketType
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
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

   private val random = Random(System.currentTimeMillis())
   private var drawerContext: CoroutineContext = SupervisorJob() + Dispatchers.Default
   private val drawerScope: CoroutineScope
      get() = CoroutineScope(drawerContext)

   val moveMutex = Mutex(locked = false)

   val lines: List<ElementLine>
      get() = elementsState.value.filterIsInstance<ElementLine>()

   val connectableElements: List<ConnectableElement>
      get() = elementsState.value.filterIsInstance<ConnectableElement>()

   val workstationElements: List<ElementWorkstation>
      get() = elementsState.value.filterIsInstance<ElementWorkstation>()

   val communicationNodeElements: List<ElementCommunicationNode>
      get() = elementsState.value.filterIsInstance<ElementCommunicationNode>()

   fun startSimulation() {
      deleteAllMessages()
      if (simulationContext.simulationPathState.value == null) return
      simulationContext.simulationStartedState.value = true
      simulationContext.simulationStoppedState.value = false
      simulationContext.next = false
      drawerContext = SupervisorJob() + Dispatchers.Default
      val simulation = configureSimulation()
      simulation.simulate(
         scope = drawerScope,
         idGenerator = {
            moveMutex.lock()
            val id = elementCounter.also { elementCounter++ }
            moveMutex.unlock()
            id
         },
         handler = { event, useErr -> handlePackage(event.mapToUiEvent(), useErr) }
      )
   }

   fun cancelAll() {
      drawerContext.cancel()
      deleteAllMessages(force = true)
      simulationContext.simulationPathState.value = null
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

   private suspend fun handlePackage(event: SimulationResultModel, useError: Boolean): Boolean {
      messageState.value = event
      when (event) {
         is SimulationResultModel.MessageStartModel -> {
            val err = handleSendEvent(event, useError)
            checkStopAndNext()
            deleteMessage(event.packetId)
            return err
         }
         SimulationResultModel.EndSimulation -> {
            simulationContext.simulationStartedState.value = false
         }
         else -> onMessageChanged(event)
      }
      return true
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
      while (simulationContext.simulationStoppedState.value) {
      }
      it.resume(Unit)
   }

   private suspend fun checkStopAndNext() {
      if (simulationContext.next) {
         stop()
      }
      checkStoppingFlag()
   }

   private suspend fun onMessageChanged(event: SimulationResultModel) {
      when (event) {
         is SimulationResultModel.TextSimulationModel -> {
         }
         is SimulationResultModel.MessageStartModel -> createNewMessage(event)
         is SimulationResultModel.MessageMoveModel -> moveMessage(event)
         is SimulationResultModel.ErrorMessageModel -> {
         }
         SimulationResultModel.EndSimulation -> deleteAllMessages()
      }
   }

   private suspend fun handleSendEvent(model: SimulationResultModel.MessageStartModel, useError: Boolean): Boolean {
      messageContext = MessageContext(
         id = model.packetId,
         toId = model.to,
         lineId = model.by,
         fromId = model.from
      )
      onMessageChanged(model)

      val line = (elementsState.value.find { it.id == model.by }) as? ElementLine ?: return false
      val fromWorkstation = (elementsState.value.find { it.id == model.from })?.center ?: return false
      val toWorkstation = (elementsState.value.find { it.id == model.to })?.center ?: return false
      val isGonnaHaveTrouble = useError and (random.nextInt(100) < line.errorChance * 100)
      val errorTick = random.nextInt(model.time.toInt())

      repeat(model.time.toInt()) {
         delay(1)
         val err = isGonnaHaveTrouble and (errorTick == it)
         onMessageChanged(
            SimulationResultModel.MessageMoveModel(
               model.packetId,
               if (err) PacketType.ERROR else model.packetType,
               model.from,
               model.to,
               model.by,
               lerp(fromWorkstation, toWorkstation, it / model.time.toFloat()),
               model.time
            )
         )
         if (err) {
            delay(100)
            deleteMessage(model.packetId)
            return false
         }
         checkStoppingFlag()
      }

      return true
   }

   private fun configureSimulation(): Simulation {
      return Simulation(
         configuration = buildConfiguration {
            path = simulationContext.simulationPathState.value!!.mapToSimulation()
            infoPacketSize = simulationContext.infoPacketSize
            sysPacketSize = simulationContext.sysPacketSize
            size = simulationContext.size
            mode = simulationContext.mode
         },
         models = elementsState.value.mapNotNull { it.mapToSimulation() }
      )
   }

   private suspend fun createNewMessage(event: SimulationResultModel.MessageStartModel) {
     moveMutex.lock()
      val fromWorkstation = elementsState.value.find { it.id == event.from } ?: kotlin.run {
         moveMutex.unlock()
         return
      }
      elementsState.value = elementsState.value.toMutableList().apply {
         add(ElementMessage(event.packetId, fromWorkstation.center, event.packetType))
      }
      moveMutex.unlock()
   }

   private suspend fun moveMessage(event: SimulationResultModel.MessageMoveModel) {
      //todo need thread safe
      moveMutex.lock()
      val index = elementsState.value.indexOfFirst { it.id == event.packetId }
      if (index == -1) {
         moveMutex.unlock()
         return
      }
      elementsState.value = elementsState.value.toMutableList().apply {
         set(index, ElementMessage(event.packetId, event.offset, event.packetType))
      }
      moveMutex.unlock()
   }

   private fun deleteAllMessages(force: Boolean = false) = runBlocking {
      if (!force) moveMutex.lock()
      elementsState.value.filter { it.type == ElementType.MESSAGE }.map { it.id }.forEach {
         deleteMessage(it)
      }
      if (!force) moveMutex.unlock()
   }

   private suspend fun deleteMessage(id: Int) {
      moveMutex.lock()
      messageContext = null
      elementsState.value.find { it.id == id }?.let {
         elementsState.value = elementsState.value.toMutableList().apply { remove(it) }
      }
      moveMutex.unlock()
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