package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import xyz.mirage.app.business.domain.models.Post
import java.io.File

@ExperimentalCoilApi
@Composable
fun PostBody(
    post: Post,
    imageLoader: ImageLoader,
) {
    post.text?.let {
        Text(
            text = it,
        )
    }
    val context = LocalContext.current
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    val simpleExoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    simpleExoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    simpleExoPlayer.playWhenReady = true
                }
                Lifecycle.Event.ON_DESTROY -> {
                    simpleExoPlayer.run {
                        stop()
                        release()
                    }
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(key1 = post) {
        onDispose {
            simpleExoPlayer.let {
                if (it.isPlaying) it.pause()
                it.release()
            }
        }
    }

    post.file?.let { file ->
        Spacer(modifier = Modifier.height(10.dp))

        when(file.filetype) {
            "video" -> {
                simpleExoPlayer.setMediaItem(MediaItem.fromUri(file.url))
                AndroidView({
                    PlayerView(context).apply {
                        setControllerVisibilityListener {
//                            onControllerVisibilityChanged(it == View.VISIBLE)
                        }
                        player = simpleExoPlayer
                    }
                },
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                )
            }
            else -> {
                val painter = rememberImagePainter(
                    data = file.url,
                    imageLoader = imageLoader,
                )

                Image(
                    painter = painter,
                    contentDescription = "Post Image for ${post.id}",
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                when (painter.state) {
                    is ImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .background(Color.Transparent)
                                .height(180.dp)
                                .fillMaxWidth()
                                .clip(shape = RoundedCornerShape(10.dp)),
                        )
                    }
                    else -> {
                    }
                }
            }
        }
    }
}