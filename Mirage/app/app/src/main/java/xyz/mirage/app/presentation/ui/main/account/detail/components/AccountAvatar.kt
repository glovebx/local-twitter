package xyz.mirage.app.presentation.ui.main.account.detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import xyz.mirage.app.presentation.core.theme.DarkBackgroundColor

@ExperimentalCoilApi
@Composable
fun UserAvatar(
    url: String,
    modifier: Modifier,
    isDarkTheme: Boolean,
    imageLoader: ImageLoader
) {
    val painter = rememberImagePainter(
        data = url,
        imageLoader = imageLoader,
    )

    Image(
        painter = painter,
        contentDescription = "Account Avatar",
        modifier = modifier
            .size(100.dp)
            .offset(20.dp, 60.dp)
            .clip(shape = CircleShape)
            .border(
                border = BorderStroke(
                    width = 3.dp,
                    color = if (isDarkTheme) DarkBackgroundColor else Color.White
                ),
                shape = CircleShape
            ),
        contentScale = ContentScale.Crop
    )

    when (painter.state) {
        is ImagePainter.State.Loading -> {
            Box(
                modifier = modifier
                    .background(Color.Transparent)
                    .size(100.dp)
                    .offset(20.dp, 60.dp)
                    .clip(shape = CircleShape)
                    .border(
                        border = BorderStroke(
                            width = 3.dp,
                            color = if (isDarkTheme) DarkBackgroundColor else Color.White
                        ),
                        shape = CircleShape
                    ),
            )
        }
        else -> {
        }
    }
}