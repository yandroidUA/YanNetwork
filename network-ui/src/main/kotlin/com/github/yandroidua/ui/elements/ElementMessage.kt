package com.github.yandroidua.ui.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.yandroidua.simulation.models.packets.PacketType
import com.github.yandroidua.ui.elements.base.ElementType
import com.github.yandroidua.ui.elements.base.ImageControlElement

class ElementMessage(
   override val id: Int,
   offset: Offset,
   packageType: PacketType
) : ImageControlElement(
   when (packageType) {
      PacketType.INFORMATION -> "message.png"
      PacketType.SYSTEM -> "sys_message.png"
      PacketType.ERROR -> "err_message.png"
   },
   offset
) {

   override val height: Int = 24
   override val width: Int = 24

   override val connectable: Boolean
      get() = false

   override val type: ElementType
      get() = ElementType.MESSAGE

}