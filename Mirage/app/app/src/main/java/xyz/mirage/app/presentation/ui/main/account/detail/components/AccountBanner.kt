package xyz.mirage.app.presentation.ui.main.account.detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@ExperimentalCoilApi
@Composable
fun UserBanner(
    url: String,
    username: String,
    imageLoader: ImageLoader
) {
    val painter = rememberImagePainter(
        data = url,
        imageLoader = imageLoader,
    )

    Image(
        painter = painter,
        contentDescription = "Banner for $username",
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth(),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center
    )

    when (painter.state) {
        is ImagePainter.State.Loading -> {
            Box(
                modifier = Modifier
                    .background(PrimaryColor)
                    .height(150.dp)
                    .fillMaxWidth(),
            )
        }
        else -> {
        }
    }
}
