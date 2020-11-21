package com.github.yandroidua.ui.components

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.SoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun EditText(
        value: String,
        onValueChange: (String) -> Unit,
        error: String? = null,
        errorIcon: String = "error.png",
        onErrorIconClicked: (String) -> Unit = { onErrIconClicked(it) },
        modifier: Modifier = Modifier,
        textStyle: TextStyle = TextStyle.Default,
        maxLines: Int = Int.MAX_VALUE,
        onImeActionPerformed: (ImeAction) -> Unit = {},
        visualTransformation: VisualTransformation = VisualTransformation.None,
        onTextLayout: (TextLayoutResult) -> Unit = {},
        onTextInputStarted: (SoftwareKeyboardController) -> Unit = {},
        cursorColor: Color = Color.Black
) = Row(modifier) {
    BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            onImeActionPerformed = onImeActionPerformed,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            onTextInputStarted = onTextInputStarted,
            cursorColor = cursorColor,
            maxLines = maxLines,
            modifier = Modifier
                    .border(
                            shape = RoundedCornerShape(size = 4.dp),
                            color = if (error.isNullOrBlank()) Color.Black else Color.Red,
                            width = 2.dp
                    )
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    )
    if (!error.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(1.dp).width(5.dp))
        Image(asset = imageFromResource(errorIcon),
                modifier = Modifier
                        .width(18.dp)
                        .height(18.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .clickable { onErrorIconClicked(error) }
        )
    }
}

@OptIn(ExperimentalKeyInput::class)
private fun onErrIconClicked(message: String) {
    AppWindow(size = IntSize(100, 100)).also {
        it.keyboard.setShortcut(Key.Escape) {
            it.close()
        }
    }.show {
        Box {
            Text(message, modifier = Modifier
                    .align(alignment = Alignment.Center)
            )
        }

    }
}