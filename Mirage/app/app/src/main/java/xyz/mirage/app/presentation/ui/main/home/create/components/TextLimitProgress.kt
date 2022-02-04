package xyz.mirage.app.presentation.ui.main.home.create.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mirage.app.presentation.core.theme.PrimaryColor
import xyz.mirage.app.presentation.core.validation.PostTextState

private val normalSize = 25.dp
private val biggerSize = 35.dp

@Composable
fun TextLimitProgress(
    text: String,
) {
    val animatedProgress = animateFloatAsState(
        targetValue = (text.length.toFloat()) / PostTextState.TEXT_MAX_LENGTH,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value

    val difference = PostTextState.TEXT_MAX_LENGTH - (text.length)
    val color = when {
        difference <= 0 -> Color.Red
        difference <= 20 -> Color.Yellow
        else -> PrimaryColor
    }

    val isClose = difference <= 20

    Box {
        CircularProgressIndicator(
            progress = 1f,
            color = Color.Gray,
            strokeWidth = 2.dp,
            modifier = Modifier
                .size((if (isClose) biggerSize else normalSize))
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearOutSlowInEasing
                    )
                )
        )

        CircularProgressIndicator(
            progress = animatedProgress,
            strokeWidth = 2.dp,
            color = color,
            modifier = Modifier
                .size((if (isClose) biggerSize else normalSize))
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearOutSlowInEasing
                    )
                )
        )

        if (isClose) {
            Text(
                text = difference.toString(),
                modifier = Modifier.align(
                    Alignment.Center
                ),
                fontSize = 13.sp
            )
        }
    }
}