package xyz.mirage.app.presentation.ui.main.home.create.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@ExperimentalComposeUiApi
@Composable
fun CreatePostAppBar(
    isDisabled: Boolean,
    handleSubmit: () -> Unit,
    handleBack: () -> Unit,
) {
    val controller = LocalSoftwareKeyboardController.current

    TopAppBar(
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        title = { Text(text = "") },
        navigationIcon = {
            IconButton(
                onClick = {
                    controller?.hide()
                    handleBack()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    tint = PrimaryColor,
                    contentDescription = "Back"
                )
            }
        }, actions = {
            Button(
                onClick = {
                    controller?.hide()
                    handleSubmit()
                },
                modifier = Modifier.padding(end = 10.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = !isDisabled,
            ) {
                Text(
                    text = "Tweet",
                    color = Color.White,
                )
            }
        }
    )
}