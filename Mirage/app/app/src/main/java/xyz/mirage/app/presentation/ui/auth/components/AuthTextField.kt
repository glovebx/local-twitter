package xyz.mirage.app.presentation.ui.auth.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import xyz.mirage.app.presentation.core.validation.PasswordState
import xyz.mirage.app.presentation.core.validation.TextFieldState
import xyz.mirage.app.presentation.ui.shared.TextFieldError

@Composable
fun AuthTextField(
    placeholder: String,
    state: TextFieldState,
    keyboardActions: KeyboardActions,
    keyboardOptions: KeyboardOptions,
    isDarkTheme: Boolean,
    requester: FocusRequester? = null,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged { focusState ->
            state.onFocusChange(focusState.isFocused)
            if (!focusState.isFocused) {
                state.enableShowErrors()
            }
        }

    if (requester != null) {
        modifier.focusRequester(requester)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
    ) {
        TextField(
            value = state.text,
            onValueChange = {
                state.text = it
                state.enableShowErrors()
            },
            modifier = modifier,
            placeholder = {
                Text(
                    text = placeholder,
                    color = if (isDarkTheme) Color.White else Color.Black,
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = state.showErrors(),
        )
    }
    state.getError()?.let { error ->
        TextFieldError(
            textError = error,
            modifier = Modifier.padding(horizontal = 50.dp)
        )
    }
}

@Composable
fun PasswordField(
    state: PasswordState,
    isDarkTheme: Boolean,
    passwordRequest: FocusRequester,
    keyboardActions: KeyboardActions,
) {
    var passwordVisibility by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
    ) {
        TextField(
            value = state.text,
            onValueChange = {
                state.text = it
                state.enableShowErrors()
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordRequest)
                .onFocusChanged { focusState ->
                    state.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        state.enableShowErrors()
                    }
                },
            placeholder = {
                Text(
                    text = "Password",
                    color = if (isDarkTheme) Color.White else Color.Black,
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = keyboardActions,
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisibility)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(
                    onClick = { passwordVisibility = !passwordVisibility }
                ) {
                    Icon(imageVector = image, "")
                }
            },
            isError = state.showErrors(),
        )
    }

    state.getError()?.let { error ->
        TextFieldError(
            textError = error,
            modifier = Modifier.padding(horizontal = 50.dp)
        )
    }
}
