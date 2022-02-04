package xyz.mirage.app.presentation.ui.main.home.create.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import xyz.mirage.app.presentation.core.theme.PrimaryColor
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostState
import xyz.mirage.app.presentation.ui.shared.TextFieldError

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@Composable
fun AvatarWithTextField(
    state: CreatePostState,
    imageLoader: ImageLoader,
) {
    val controller = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        state.account?.let { account ->

            val painter = rememberImagePainter(
                data = account.image,
                imageLoader = imageLoader,
            )

            Image(
                painter = painter,
                contentDescription = "Avatar",
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(40.dp)
                    .clip(shape = CircleShape),
                contentScale = ContentScale.Crop
            )

            when (painter.state) {
                is ImagePainter.State.Loading -> {
                    Box(
                        modifier = Modifier
                            .background(PrimaryColor)
                            .padding(top = 10.dp)
                            .size(40.dp)
                            .clip(shape = CircleShape),
                    )
                }
                else -> {
                }
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        TextField(
            value = state.text.text,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { text -> state.text.text = text },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { controller?.hide() }
            ),
            placeholder = {
                Text(
                    text = if (state.uri != null) "Add a comment..." else "What's happening?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
        )

        state.text.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}