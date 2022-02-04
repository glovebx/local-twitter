package xyz.mirage.app.presentation.ui.main.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.main.home.list.components.PostListItem
import xyz.mirage.app.presentation.ui.main.profile.components.ListItem
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.SetDeletePostInList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.SetRefreshPostInList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import xyz.mirage.app.presentation.ui.main.search.SearchEvents.*
import xyz.mirage.app.presentation.ui.main.search.components.ProfileListItem
import xyz.mirage.app.presentation.ui.main.search.components.SearchAppBar
import xyz.mirage.app.presentation.ui.shared.CustomDivider
import xyz.mirage.app.presentation.ui.shared.EmptyPlaceholder
import xyz.mirage.app.presentation.ui.shared.NavBar

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    isDarkTheme: Boolean,
    isNetworkAvailable: Boolean,
    navController: NavController,
    state: SearchState,
    onTriggerEvent: (SearchEvents) -> Unit,
    scaffoldState: ScaffoldState,
    refreshViewManager: RefreshViewManager,
    authId: String,
    imageLoader: ImageLoader,
) {
    val listState = rememberLazyListState()

    refreshViewManager.state.value.let { value ->
        value.postToRefresh?.let { post ->
            onTriggerEvent(UpdateListEvent(post))
            refreshViewManager.onTriggerEvent(SetRefreshPostInList(null))
        }

        value.postToRemove?.let { id ->
            onTriggerEvent(RemoveItemEvent(id))
            refreshViewManager.onTriggerEvent(SetDeletePostInList(null))
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
                SearchAppBar(
                    query = state.query,
                    onQueryChanged = { onTriggerEvent(UpdateQuery(it)) },
                    onExecuteSearch = { onTriggerEvent(NewSearch) },
                    isDarkTheme = isDarkTheme
                )
            },
            scaffoldState = scaffoldState,
            bottomBar = {
                NavBar(
                    navController = navController,
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp, top = 10.dp)
            ) {
                LazyColumn(
                    state = listState
                ) {
                    item {
                        CustomDivider(isDarkTheme = isDarkTheme)
                    }

                    when {
                        state.profiles.isEmpty() && state.posts.isEmpty() -> {
                            item {
                                EmptyPlaceholder(text = "No results for\nthe given search")
                            }
                        }
                        state.profiles.isNotEmpty() -> {
                            itemsIndexed(
                                items = state.profiles,
                            ) { index, profile ->
                                onTriggerEvent(OnUpdateScrollPosition(index))

                                ProfileListItem(
                                    profile = profile,
                                    onNavigateToProfileScreen = {
                                        val route = Screen.Profile.route + "/${profile.username}"
                                        navController.navigate(route)
                                    },
                                    imageLoader = imageLoader
                                )
                                CustomDivider(isDarkTheme = isDarkTheme)
                            }
                        }
                        state.posts.isNotEmpty() -> {
                            itemsIndexed(
                                items = state.posts,
                            ) { index, post ->
                                ListItem(
                                    index = index,
                                    page = state.page,
                                    deletedCount = 0,
                                    isLoading = state.isLoading,
                                    isDarkTheme = isDarkTheme,
                                    onChangeScrollPosition = {
                                        onTriggerEvent(
                                            OnUpdateScrollPosition(
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
                                        onToggleLike = { onTriggerEvent(ToggleLikeEvent(it)) },
                                        onToggleRetweet = { onTriggerEvent(ToggleRetweetEvent(it)) },
                                        onToggleFollow = { onTriggerEvent(OnToggleFollow(it)) },
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
}