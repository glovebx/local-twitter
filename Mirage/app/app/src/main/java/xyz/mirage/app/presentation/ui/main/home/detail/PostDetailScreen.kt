package xyz.mirage.app.presentation.ui.main.home.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.main.home.detail.components.PostDetailAppBar
import xyz.mirage.app.presentation.ui.main.home.detail.components.PostView
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import xyz.mirage.app.presentation.ui.shared.CircularIndeterminateProgressBar

@ExperimentalCoilApi
@Composable
fun PostDetailScreen(
    isDarkTheme: Boolean,
    isNetworkAvailable: Boolean,
    postId: String?,
    onTriggerEvent: (PostEvent) -> Unit,
    state: PostDetailState,
    navController: NavController,
    scaffoldState: ScaffoldState,
    refreshViewManager: RefreshViewManager,
    authId: String,
    imageLoader: ImageLoader,
) {
    if (!state.onLoad) {
        onTriggerEvent(PostEvent.OnToggleOnLoad)
        postId?.let { id ->
            onTriggerEvent(PostEvent.GetPostEvent(id))
        }
    }

    if (refreshViewManager.state.value.postToRemove == postId) {
        navController.popBackStack()
    }

    AppTheme(
        darkTheme = isDarkTheme,
        displayProgressBar = state.isLoading,
        scaffoldState = scaffoldState,
        isNetworkAvailable = isNetworkAvailable,
        dialogQueue = state.queue,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(PostEvent.OnRemoveHeadFromQueue)
        }
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                PostDetailAppBar(
                    handleBack = { navController.popBackStack() }
                )
            },
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                state.post?.let { post ->
                    PostView(
                        post = post,
                        isDarkTheme = isDarkTheme,
                        onNavigateToProfileScreen = {
                            val route = Screen.Profile.route + "/${post.profile.username}"
                            navController.navigate(route)
                        },
                        authId = authId,
                        onTriggerEvent = onTriggerEvent,
                        imageLoader = imageLoader
                    )
                } ?: CircularIndeterminateProgressBar(isDisplayed = state.isLoading)
            }
        }
    }
}