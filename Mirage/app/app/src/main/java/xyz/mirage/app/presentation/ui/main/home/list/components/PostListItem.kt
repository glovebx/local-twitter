package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.android.exoplayer2.SimpleExoPlayer
import xyz.mirage.app.business.domain.models.Post

@ExperimentalCoilApi
@Composable
fun PostListItem(
    post: Post,
    onNavigateToPostScreen: () -> Unit,
    onNavigateToProfileScreen: () -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleRetweet: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onToggleDelete: (String) -> Unit,
    authId: String,
    isDarkTheme: Boolean,
    imageLoader: ImageLoader,
) {
    Column(
        modifier = Modifier
            .padding(all = 10.dp)
            .clickable {
                onNavigateToPostScreen()
            }
    ) {
        if (post.isRetweet) {
            RetweetLabel(
                isDarkTheme = isDarkTheme
            )
        }

        Row {
            Avatar(
                profile = post.profile,
                onNavigateToProfileScreen = onNavigateToProfileScreen,
                imageLoader = imageLoader
            )
            Spacer(
                modifier = Modifier.size(12.dp)
            )
            Column {
                Username(
                    post = post,
                    authId = authId,
                    handleFollow = { onToggleFollow(it) },
                    handleDeletePost = { onToggleDelete(it) },
                )
                PostBody(
                    post = post,
                    imageLoader = imageLoader,
                )
                Spacer(modifier = Modifier.size(10.dp))
                PostActions(
                    post = post,
                    onToggleLike = onToggleLike,
                    onToggleRetweet = onToggleRetweet,
                )
            }
        }
    }
}