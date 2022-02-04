package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.main.home.list.PostListEvent
import xyz.mirage.app.presentation.ui.main.home.list.PostListEvent.*
import xyz.mirage.app.presentation.ui.main.home.list.PostListState
import xyz.mirage.app.presentation.ui.main.profile.components.ListItem
import xyz.mirage.app.presentation.ui.shared.EmptyPlaceholder

@ExperimentalCoilApi
@Composable
fun PostList(
    state: PostListState,
    navController: NavController,
    listState: LazyListState,
    isDarkTheme: Boolean,
    onTriggerEvent: (PostListEvent) -> Unit,
    authId: String,
    imageLoader: ImageLoader,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp)
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = state.isRefreshing),
            onRefresh = { onTriggerEvent(FeedEvent) }
        ) {
            when {
                state.posts.isEmpty() && !state.isLoading -> {
                    EmptyPlaceholder(
                        text = "No posts here yet.\n Follow some profiles or post something"
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState
                    ) {
                        itemsIndexed(
                            items = state.posts,
                        ) { index, post ->
                            ListItem(
                                index = index,
                                page = state.page,
                                deletedCount = state.deletedCount,
                                isLoading = state.isLoading,
                                isDarkTheme = isDarkTheme,
                                onChangeScrollPosition = {
                                    onTriggerEvent(
                                        ChangeScrollPositionEvent(
                                            index
                                        )
                                    )
                                },
                                fetchNextPage = { onTriggerEvent(NextPageEvent) }
                            ) {
                                PostListItem(
                                    post = post,
                                    onNavigateToPostScreen = {
                                        val route = Screen.PostDetail.route + "/${post.id}"
                                        navController.navigate(route)
                                    },
                                    onNavigateToProfileScreen = {
                                        val route =
                                            Screen.Profile.route + "/${post.profile.username}"
                                        navController.navigate(route)
                                    },
                                    onToggleRetweet = { onTriggerEvent(ToggleRetweetEvent(it)) },
                                    onToggleLike = { onTriggerEvent(ToggleLikeEvent(it)) },
                                    onToggleFollow = { onTriggerEvent(ToggleFollowEvent(it)) },
                                    onToggleDelete = { onTriggerEvent(DeletePostEvent(it)) },
                                    authId = authId,
                                    isDarkTheme = isDarkTheme,
                                    imageLoader = imageLoader,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}