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
import xyz.mirage.app.presentation.ui.main.home.list.components.RetweetAnimationDefinition.expandedIconSize
import xyz.mirage.app.presentation.ui.main.home.list.components.RetweetAnimationDefinition.idleIconSize

@Composable
fun AnimatedRetweetButton(
    modifier: Modifier,
    isRetweeted: Boolean,
    onToggle: () -> Unit
) {

    val size by animateDpAsState(
        // Animation won't work if target value is always equal, this may be a jetpack compose bug?
        if (isRetweeted) 20.1.dp else 20.dp,
        animationSpec = keyframes {
            durationMillis = 500
            expandedIconSize.at(100)
            idleIconSize.at(200)
        },
    )

    RetweetButton(
        modifier = modifier,
        isRetweeted = isRetweeted,
        onToggle = onToggle,
        size = size
    )
}

@Composable
private fun RetweetButton(
    modifier: Modifier,
    isRetweeted: Boolean,
    onToggle: () -> Unit,
    size: Dp
) {
    if (isRetweeted) {
        Image(
            painter = painterResource(id = R.drawable.ic_retweeted),
            modifier = modifier
                .size(size)
                .clickable(onClick = onToggle),
            contentDescription = "Retweeted post"
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_retweet),
            modifier = modifier
                .size(size)
                .clickable(onClick = onToggle),
            contentDescription = "Retweet post"
        )
    }
}

object RetweetAnimationDefinition {
    val idleIconSize = 20.dp
    val expandedIconSize = 30.dp
}