package xyz.mirage.app.presentation.ui.main.home.list

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.launch
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.main.home.list.PostListEvent.*
import xyz.mirage.app.presentation.ui.main.home.list.components.PostList
import xyz.mirage.app.presentation.ui.main.home.list.components.TopBar
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import xyz.mirage.app.presentation.ui.shared.CreatePostFab
import xyz.mirage.app.presentation.ui.shared.NavBar

@ExperimentalCoilApi
@Composable
fun HomeScreen(
    isDarkTheme: Boolean,
    isNetworkAvailable: Boolean,
    navController: NavController,
    state: PostListState,
    onTriggerEvent: (PostListEvent) -> Unit,
    scaffoldState: ScaffoldState,
    refreshViewManager: RefreshViewManager,
    authId: String,
    imageLoader: ImageLoader,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    refreshViewManager.state.value.let { value ->
        value.postToRefresh?.let { post ->
            onTriggerEvent(UpdateListEvent(post))
        }

        value.postToRemove?.let { id ->
            onTriggerEvent(RemoveItemEvent(id))
        }

        value.postToAdd?.let { post ->
            onTriggerEvent(AddPostToListEvent(post))
        }
    }

    AppTheme(
        displayProgressBar = state.isLoading,
        scaffoldState = scaffoldState,
        darkTheme = isDarkTheme,
        isNetworkAvailable = isNetworkAvailable,
        dialogQueue = state.queue,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(OnRemoveHeadFromQueue)
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    onScrollToTop = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    isDarkTheme = isDarkTheme,
                )
            },
//            floatingActionButton = {
//                CreatePostFab(
//                    handleClick = { navController.navigate(Screen.CreatePost.route) }
//                )
//            },
            bottomBar = {
                NavBar(
                    navController = navController,
                )
            }
        ) {
            PostList(
                state = state,
                navController = navController,
                listState = listState,
                isDarkTheme = isDarkTheme,
                onTriggerEvent = onTriggerEvent,
                authId = authId,
                imageLoader = imageLoader,
            )
        }
    }
}