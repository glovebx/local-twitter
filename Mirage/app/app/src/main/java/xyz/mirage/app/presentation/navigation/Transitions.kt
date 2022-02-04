package xyz.mirage.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

private const val duration = 300

@ExperimentalAnimationApi
fun slideExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -duration },
        animationSpec = tween(duration)
    ) + fadeOut(animationSpec = tween(duration))
}

@ExperimentalAnimationApi
fun slidePopEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -duration },
        animationSpec = tween(duration)
    ) + fadeIn(animationSpec = tween(duration))
}

@ExperimentalAnimationApi
fun slideEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { duration },
        animationSpec = tween(duration)
    ) + fadeIn(animationSpec = tween(duration))
}

@ExperimentalAnimationApi
fun slidePopExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { duration },
        animationSpec = tween(duration)
    ) + fadeOut(animationSpec = tween(duration))
}