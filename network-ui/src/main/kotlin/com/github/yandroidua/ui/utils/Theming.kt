package com.github.yandroidua.ui.utils

import androidx.compose.material.ButtonColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.graphics.Color

object VovaTheming {

   @OptIn(ExperimentalMaterialApi::class)
   fun buttonColors(): ButtonColors = object : ButtonColors {
      override fun backgroundColor(enabled: Boolean): Color {
         return if (enabled) Color(0xFF009688) else Color(0x80009688)
      }
      override fun contentColor(enabled: Boolean): Color {
         return Color.White
      }
   }

}