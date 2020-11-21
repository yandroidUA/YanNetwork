package com.github.yandroidua.ui.screens.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.unit.dp
import com.github.yandroidua.ui.elements.Line

private data class LineDetailsErrorState(
        val weightErrorMessage: String? = null
)

private data class Input(
        val weight: String = ""
)

@Composable
fun LineDetails(modifier: Modifier = Modifier, line: Line) = Column(modifier) {
    val inputState = remember { mutableStateOf(Input(weight = line.weight.toString())) }
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
            BasicTextField(
                    value = inputState.value.weight,
                    onValueChange = { weight ->
                        inputState.value = inputState.value.copy(weight = weight)
                        errorState.value = errorState.value.copy(weightErrorMessage = null)
                    },
                    maxLines = 1,
                    modifier = Modifier
                            .border(
                                    shape = RoundedCornerShape(size = 4.dp),
                                    color = if (errorState.value.weightErrorMessage.isNullOrBlank()) Color.Black else Color.Red,
                                    width = 2.dp
                            )
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            )
            if (!errorState.value.weightErrorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(1.dp).width(5.dp))
                Image(asset = imageFromResource("error.png"),
                        modifier = Modifier
                                .width(18.dp)
                                .height(18.dp)
                                .align(alignment = Alignment.CenterVertically)
                )
            }
        }

    }
    Button(onClick = { checkInput(errorState, inputState.value) }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Save", modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally))
    }

}

private fun checkInput(errorState: MutableState<LineDetailsErrorState>, input: Input) {
    val lineError = checkWeight(input.weight)
    //todo other checks
    errorState.value = LineDetailsErrorState(weightErrorMessage = lineError)
}

private fun checkWeight(weight: String): String? {
    return try {
        val num = weight.toInt()
        if (num > 0) null else "Wrong number"
    } catch (e : NumberFormatException) {
//        e.printStackTrace()
        "Wrong number"
    }
}