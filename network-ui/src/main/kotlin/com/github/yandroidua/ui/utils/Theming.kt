package com.github.yandroidua.ui.utils

import androidx.compose.material.ButtonColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.graphics.Color

object DaeerTheming {

   @OptIn(ExperimentalMaterialApi::class)
   fun buttonColors(): ButtonColors = object : ButtonColors {
      override fun backgroundColor(enabled: Boolean): Color {
         return if (enabled) Color(151, 115,255) else Color(255, 208, 220)
      }
      override fun contentColor(enabled: Boolean): Color {
         return Color.White
      }
   }
}