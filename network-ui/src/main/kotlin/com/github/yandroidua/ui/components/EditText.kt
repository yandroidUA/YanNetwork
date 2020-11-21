package com.github.yandroidua.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun EditText(
        value: String,
        onValueChange: (String) -> Unit,
        error: String? = null,
        errorIcon: String = "error.png",
        onErrorIconClicked: (String) -> Unit = { onErrIconClicked(it) },
        modifier: Modifier = Modifier,
        textStyle: TextStyle = TextStyle.Default,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        maxLines: Int = Int.MAX_VALUE,
        onImeActionPerformed: (ImeAction) -> Unit = {},
        visualTransformation: VisualTransformation = VisualTransformation.None,
        onTextLayout: (TextLayoutResult) -> Unit = {},
        onTextInputStarted: (SoftwareKeyboardController) -> Unit = {},
        cursorColor: Color = Color.Black
) = Row {

}

private fun onErrIconClicked(message: String) {

}