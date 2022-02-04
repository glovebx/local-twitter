package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.mirage.app.R

@Composable
fun AnimatedHeartButton(
    modifier: Modifier,
    isLiked: Boolean,
    onToggle: () -> Unit
) {

    val size by animateDpAsState(
        // Animation won't work if target value is always equal, this may be a jetpack compose bug?
        if (isLiked) 20.1.dp else 20.dp,
        animationSpec = keyframes {
            durationMillis = 500
            HeartAnimationDefinition.expandedIconSize.at(100)
            HeartAnimationDefinition.idleIconSize.at(200)
        },
    )

    HeartButton(
        modifier = modifier,
        isLiked = isLiked,
        onToggle = onToggle,
        size = size
    )
}

@Composable
private fun HeartButton(
    modifier: Modifier,
    isLiked: Boolean,
    onToggle: () -> Unit,
    size: Dp
) {
    if (isLiked) {
        Image(
            painter = painterResource(id = R.drawable.ic_liked),
            modifier = modifier
                .size(size)
                .clickable(onClick = onToggle),
            contentDescription = "Liked post"
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_like),
            modifier = modifier
                .size(size)
                .clickable(onClick = onToggle),
            contentDescription = "Unliked post"
        )
    }
}

object HeartAnimationDefinition {
    val idleIconSize = 20.dp
    val expandedIconSize = 30.dp
}