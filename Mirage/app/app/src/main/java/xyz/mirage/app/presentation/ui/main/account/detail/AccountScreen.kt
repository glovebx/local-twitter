package xyz.mirage.app.presentation.ui.main.account.detail

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import xyz.mirage.app.business.datasources.datastore.SettingsDataStore
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.main.account.detail.AccountEvents.*
import xyz.mirage.app.presentation.ui.main.account.detail.components.AccountAppBar
import xyz.mirage.app.presentation.ui.main.account.detail.components.AccountInfo
import xyz.mirage.app.presentation.ui.main.account.detail.components.ThemeDialog
import xyz.mirage.app.presentation.ui.main.home.list.components.PostListItem
import xyz.mirage.app.presentation.ui.main.profile.components.*
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.*
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import xyz.mirage.app.presentation.ui.shared.CircularIndeterminateProgressBar
import xyz.mirage.app.presentation.ui.shared.EmptyPlaceholder
import xyz.mirage.app.presentation.ui.shared.NavBar

@ExperimentalCoilApi
@Composable
fun AccountScreen(
    isDarkTheme: Boolean,
    state: AccountState,
    onTriggerEvent: (AccountEvents) -> Unit,
    navController: NavController,
    settingsDataStore: SettingsDataStore,
    isNetworkAvailable: Boolean,
    imageLoader: ImageLoader,
    scaffoldState: ScaffoldState,
    refreshViewManager: RefreshViewManager
) {
    val coroutineScope = rememberCoroutineScope()
    val showThemeDialog = remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    if (refreshViewManager.state.value.shouldRefreshAccount) {
        onTriggerEvent(GetAccount)
        refreshViewManager.onTriggerEvent(SetRefreshAccount(false))
    }

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
        darkTheme = isDarkTheme,
        dialogQueue = state.queue,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(OnRemoveHeadFromQueue)
        },
        isNetworkAvailable = isNetworkAvailable,
        scaffoldState = scaffoldState,
        displayProgressBar = state.isLoading,
    ) {
        Scaffold(
            topBar = {
                AccountAppBar(
                    handleLogout = { onTriggerEvent(OnLogout) },
                    scrollState = scrollState,
                    isDarkTheme = isDarkTheme,
                    onScrollToTop = {
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    },
                )
            },
            bottomBar = {
                NavBar(
                    navController = navController,
                )
            }
        ) {
            state.account?.let { user ->
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = state.isRefreshing),
                    onRefresh = { onTriggerEvent(OnChangeTabEvent(state.currentTab)) }
                ) {
                    LazyColumn(
                        // Pull up content to appbar height
                        modifier = Modifier.offset(0.dp, (-60).dp),
                        state = scrollState
                    ) {
                        item {
                            AccountInfo(
                                user = user,
                                isDarkTheme = isDarkTheme,
                                openDialog = { showThemeDialog.value = true },
                                handleNavigation = {
                                    navController.navigate(Screen.UpdateAccount.route + "/${user.id}")
                                },
                                imageLoader = imageLoader
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.size(60.dp))
                        }

                        item {
                            DisplayName(displayName = user.displayName)
                        }

                        item {
                            Username(username = user.username)
                        }

                        user.bio?.let { bio ->
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Biography(bio = bio)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            PostsTabs(
                                currentTab = state.currentTab,
                                onChangeTab = { tab ->
                                    onTriggerEvent(
                                        OnChangeTabEvent(tab)
                                    )
                                },
                                isDarkTheme = isDarkTheme,
                            )
                        }

                        when {
                            state.posts.isEmpty() -> {
                                item {
                                    EmptyPlaceholder(text = "Nothing to see here")
                                }
                            }
                            else -> {
                                itemsIndexed(
                                    items = state.posts,
                                ) { index, post ->
                                    ListItem(
                                        index = index,
                                        page = state.page,
                                        deletedCount = 0,
                                        isLoading = state.postsIsLoading,
                                        isDarkTheme = isDarkTheme,
                                        onChangeScrollPosition = {
                                            onTriggerEvent(
                                                OnChangePostScrollPositionEvent(index)
                                            )
                                        },
                                        fetchNextPage = { onTriggerEvent(NextPageEvent) }
                                    ) {
                                        PostListItem(
                                            post = post,
                                            onNavigateToPostScreen = {
                                                val route = Screen.PostDetail.route + "/${post.id}"
                                                navController.navigate(route = route)
                                            },
                                            onNavigateToProfileScreen = {
                                                val route =
                                                    Screen.Profile.route + "/${post.profile.username}"
                                                navController.navigate(route)
                                            },
                                            onToggleLike = { onTriggerEvent(ToggleLikeEvent(it)) },
                                            onToggleRetweet = {
                                                onTriggerEvent(ToggleRetweetEvent(it))
                                            },
                                            onToggleFollow = { onTriggerEvent(ToggleFollowEvent(it)) },
                                            onToggleDelete = { onTriggerEvent(DeletePostEvent(it)) },
                                            authId = user.id,
                                            isDarkTheme = isDarkTheme,
                                            imageLoader = imageLoader,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: CircularIndeterminateProgressBar(isDisplayed = state.isLoading)

            ThemeDialog(
                showThemeDialog = showThemeDialog,
                isDarkTheme = isDarkTheme,
                toggleTheme = { settingsDataStore.toggleTheme() }
            )
        }
    }
}