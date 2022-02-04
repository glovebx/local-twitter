package xyz.mirage.app.presentation.ui.main.home.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.presentation.ui.main.home.detail.PostEvent
import xyz.mirage.app.presentation.ui.main.home.list.components.AnimatedHeartButton
import xyz.mirage.app.presentation.ui.main.home.list.components.AnimatedRetweetButton

@Composable
fun PostActions(
    post: Post,
    onTriggerEvent: (PostEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedRetweetButton(
            modifier = Modifier,
            isRetweeted = post.retweeted,
            onToggle = {
                onTriggerEvent(PostEvent.ToggleRetweetEvent(post.id))
            }
        )

        AnimatedHeartButton(
            modifier = Modifier,
            isLiked = post.liked,
            onToggle = {
                onTriggerEvent(PostEvent.ToggleLikeEvent(post.id))
            }
        )
    }
}