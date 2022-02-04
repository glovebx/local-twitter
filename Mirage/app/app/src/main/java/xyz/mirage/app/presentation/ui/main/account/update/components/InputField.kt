package xyz.mirage.app.presentation.ui.main.account.update.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mirage.app.presentation.core.theme.PrimaryColor
import xyz.mirage.app.presentation.core.theme.RedErrorLight
import xyz.mirage.app.presentation.core.validation.TextFieldState

@Composable
fun InputField(
    label: String,
    state: TextFieldState,
    keyboardActions: KeyboardActions,
    keyboardOptions: KeyboardOptions,
    isDarkTheme: Boolean,
    maxLines: Int = 1,
    onTriggerEvent: () -> Unit,
) {
    val dividerColor = when {
        !state.isValid -> RedErrorLight
        state.isFocused -> PrimaryColor
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = state.text,
            maxLines = maxLines,
            onValueChange = {
                state.text = it
                state.enableShowErrors()
                onTriggerEvent()
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    state.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        state.enableShowErrors()
                    }
                },
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = if (isDarkTheme) Color.White else Color.Black,
            ),
            decorationBox = { innerTextField ->
                Column {
                    Text(
                        text = label,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )

                    Box(
                        modifier = Modifier.padding(
                            top = 5.dp, bottom = 12.dp
                        )
                    ) {
                        innerTextField()
                    }

                    Divider(
                        color = dividerColor,
                        thickness = if (state.isFocused) 1.dp else 0.8.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = state.getError() ?: "",
                            color = RedErrorLight,
                            fontSize = 13.sp
                        )

                        state.max?.let { maxLength ->
                            Text(
                                "${state.text.length}/${maxLength}",
                                color = if (!state.isValid) RedErrorLight else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            cursorBrush = SolidColor(PrimaryColor),
        )
    }
}