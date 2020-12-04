package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.yandroidua.simulation.models.LineType
import com.github.yandroidua.ui.components.EditText
import com.github.yandroidua.ui.elements.ElementLine

private data class LineDetailsErrorState(
    val weightErrorMessage: String? = null
)

private data class Input(
    val weight: String = "",
    val dropDownExpanded: Boolean = false,
    val lineType: LineType = LineType.DUPLEX
)

@Composable
fun LineDetails(
    modifier: Modifier = Modifier,
    elementLine: ElementLine,
    deleter: (ElementLine) -> Unit,
    saver: (ElementLine) -> Unit
) = Column(modifier) {
   val inputState = remember { mutableStateOf(Input(weight = elementLine.weight.toString())) }
   val errorState = remember { mutableStateOf(LineDetailsErrorState()) }
   Column(modifier = Modifier.weight(1f)) {
      Text(
          text = "This is Line",
          modifier = Modifier
              .wrapContentWidth(align = Alignment.CenterHorizontally)
              .align(alignment = Alignment.CenterHorizontally)
      )
      Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
      Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top)) {
         Text(text = "Weight", modifier = Modifier.align(alignment = Alignment.CenterVertically))
         Spacer(modifier = Modifier.height(1.dp).width(5.dp))
         EditText(
             value = inputState.value.weight,
             onValueChange = { weight ->
                 inputState.value = inputState.value.copy(weight = weight)
                 errorState.value = errorState.value.copy(weightErrorMessage = null)
             },
             error = errorState.value.weightErrorMessage,
             maxLines = 1
         )
      }
      Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
      Row {
         Text(text = "Line type:")
         Spacer(modifier = Modifier.height(1.dp).width(5.dp))
         DropdownMenu(
             toggleModifier = Modifier.wrapContentSize(),
             dropdownModifier = Modifier.wrapContentSize(),
             toggle = {
                 Text(inputState.value.lineType.name, modifier = Modifier
                     .clickable { inputState.value = inputState.value.copy(dropDownExpanded = !inputState.value.dropDownExpanded) })
             },
             expanded = inputState.value.dropDownExpanded,
             onDismissRequest = { inputState.value = inputState.value.copy(dropDownExpanded = false) }
         ) {
            for (lineType in LineType.values()) {
               DropdownMenuItem(
                   onClick = {
                       inputState.value = inputState.value.copy(dropDownExpanded = false, lineType = lineType)
                   }
               ) { Text(text = lineType.name) }
            }
         }
      }

   }
   Button(
       modifier = Modifier.fillMaxWidth(),
       onClick = { deleter(elementLine) }
   ) { Text(text = "Delete") }
   Spacer(modifier = Modifier.height(height = 20.dp))
   Button(onClick = {
       checkInput(
           errorState,
           inputState.value,
           onValid = {
               saver(elementLine.copy(
                   weight = it.weight.toInt(),
                   lineType = inputState.value.lineType,
                   color = colorOfType(inputState.value.lineType)
               ))
           }
       )
   },
       modifier = Modifier.fillMaxWidth()
   ) {
      Text(text = "Save", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
   }

}

private fun checkInput(errorState: MutableState<LineDetailsErrorState>, input: Input, onValid: (Input) -> Unit) {
   var valid = true
   val lineError = checkWeight(input.weight)
   valid = valid and lineError.isNullOrBlank()
   errorState.value = LineDetailsErrorState(weightErrorMessage = lineError)
   if (valid) {
      onValid(input)
   }
}

private fun checkWeight(weight: String): String? {
   return try {
      val num = weight.toInt()
      if (num > 0) null else "Number must be greater than 0"
   } catch (e: NumberFormatException) {
      "Wrong number format"
   }
}

private fun colorOfType(type: LineType): Color {
   return when (type) {
       LineType.DUPLEX -> Color.Black
       LineType.HALF_DUPLEX -> Color.Blue
       LineType.SATELLITE -> Color.Red
   }
}
