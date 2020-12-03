package com.github.yandroidua.ui.screens.drawer

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import com.github.yandroidua.dump.models.CommunicationNodeDump
import com.github.yandroidua.ui.elements.ElementCommunicationNode
import com.github.yandroidua.ui.elements.ElementLine
import com.github.yandroidua.ui.elements.ElementMessage
import com.github.yandroidua.ui.elements.ElementWorkstation
import com.github.yandroidua.ui.elements.base.ConnectableElement
import com.github.yandroidua.ui.elements.base.Element
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.models.PathResultElements
import com.github.yandroidua.ui.models.SimulationResultModel
import kotlinx.coroutines.Job

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
     * Determine step from start simulation
     */
    var simulationStartStep: Int = 0,
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
     * Contains path that need to be simulated
     */
    var pathToSimulate: PathResultElements? = null,
    /**
     * Contains current simulation job
     */
    var simulationJob: Job? = null
) {

   val lines: List<ElementLine>
      get() = elementsState.value.filterIsInstance<ElementLine>()

   val connectableElements: List<ConnectableElement>
      get() = elementsState.value.filterIsInstance<ConnectableElement>()

   val workstationElements: List<ElementWorkstation>
      get() = elementsState.value.filterIsInstance<ElementWorkstation>()

   val communicationNodeElements: List<ElementCommunicationNode>
      get() = elementsState.value.filterIsInstance<ElementCommunicationNode>()

   fun undo() {
      selectedElementType = null
      lineCreationLastTouchOffset = null
      selectedElementState.value = null
      removeElement(elementsState.value.lastOrNull() ?: return)
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

   /**
    *
    * private fun PanelPageContext.onCanvasTyped(
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
    *
    */

   fun onCanvasTyped(position: Offset, onElementDetected: (Element) -> Unit) {
      //todo handle all detection && creation && e.t.c.
   }

   private fun removeWorkstation(elementWorkstation: ElementWorkstation) {
      removeConnectableElement(elementWorkstation)
   }

   private fun removeCommunicationNode(communicationNode: ElementCommunicationNode) {
     removeConnectableElement(communicationNode)
   }

   private fun removeConnectableElement(connectableElement: ConnectableElement) {
      // searching connected lines to this workstation
      connectableElement.lineIds.forEach { lineId ->
         (elementsState.value.find { it.id == lineId } as? ElementLine)?.let(this::removeLine)
      }
      elementsState.value = elementsState.value.toMutableList().apply { remove(connectableElement) }
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