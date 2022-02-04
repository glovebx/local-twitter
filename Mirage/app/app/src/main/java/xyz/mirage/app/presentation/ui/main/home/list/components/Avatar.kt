package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import xyz.mirage.app.business.domain.models.Profile

@ExperimentalCoilApi
@Composable
fun Avatar(
    profile: Profile,
    imageLoader: ImageLoader,
    onNavigateToProfileScreen: () -> Unit
) {

    val painter = rememberImagePainter(
        data = profile.image,
        imageLoader = imageLoader,
    )

    Image(
        painter = painter,
        modifier = Modifier
            .size(50.dp)
            .clip(shape = CircleShape)
            .clickable {
                onNavigateToProfileScreen()
            },
        contentDescription = "Avatar for ${profile.username}",
        contentScale = ContentScale.Crop
    )

    when (painter.state) {
        is ImagePainter.State.Loading -> {
            Box(
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .background(Color.Transparent)
            )
        }
        else -> {
        }
    }
}