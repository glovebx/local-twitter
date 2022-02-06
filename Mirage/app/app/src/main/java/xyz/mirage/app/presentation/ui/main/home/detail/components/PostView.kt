package xyz.mirage.app.presentation.ui.main.home.detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.presentation.core.util.DateUtils
import xyz.mirage.app.presentation.ui.main.home.detail.PostEvent
import xyz.mirage.app.presentation.ui.main.home.detail.PostEvent.DeletePostEvent
import xyz.mirage.app.presentation.ui.main.home.detail.PostEvent.ToggleFollowEvent
import xyz.mirage.app.presentation.ui.main.home.list.components.Avatar
import xyz.mirage.app.presentation.ui.shared.DividerWithSpace

@ExperimentalCoilApi
@Composable
fun PostView(
    post: Post,
    imageLoader: ImageLoader,
    isDarkTheme: Boolean,
    onNavigateToProfileScreen: () -> Unit,
    onTriggerEvent: (PostEvent) -> Unit,
    authId: String,
) {
    var showMenu by remember { mutableStateOf(false) }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Avatar(
                        profile = post.profile,
                        onNavigateToProfileScreen = onNavigateToProfileScreen,
                        imageLoader = imageLoader
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    Column {
                        Text(
                            text = post.profile.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = "@${post.profile.username}",
                            color = Color.Gray,
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = {
                            showMenu = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = Color.Gray,
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        if (authId != post.profile.id) {
                            DropdownMenuItem(
                                onClick = {
                                    onTriggerEvent(ToggleFollowEvent(post.profile.username))
                                    showMenu = false
                                }
                            ) {
                                Text(text = if (post.profile.following) "Unfollow" else "Follow")
                            }
                        } else {
                            DropdownMenuItem(
                                onClick = {
                                    onTriggerEvent(DeletePostEvent(post.id))
                                    showMenu = false
                                }
                            ) {
                                Text(text = "Delete")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            post.text?.let {
                Text(
                    text = it,
                    fontSize = 18.sp,
                )
            }
        }


        item {
            post.file?.let { file ->

                Spacer(modifier = Modifier.height(10.dp))

                when(file.filetype) {
                    "video" -> {
                        simpleExoPlayer.apply {
                            setMediaItem(MediaItem.fromUri(file.url))
                            prepare()
                            playWhenReady = true
                        }
                        AndroidView(
                            {
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
                                .heightIn(100.dp)
                                .fillMaxWidth()
                                .clip(shape = RoundedCornerShape(15.dp)),
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.size(10.dp))
        }

        item {
            Text(
                text = DateUtils.formatDate(post.createdAt),
                color = Color.Gray,
            )
        }

        item {
            DividerWithSpace(isDarkTheme = isDarkTheme)
        }

        item {
            PostStats(post = post)
        }

        item {
            DividerWithSpace(isDarkTheme = isDarkTheme)
        }

        item {
            PostActions(
                post = post,
                onTriggerEvent = onTriggerEvent
            )
        }

        item {
            DividerWithSpace(isDarkTheme = isDarkTheme)
        }
    }
}