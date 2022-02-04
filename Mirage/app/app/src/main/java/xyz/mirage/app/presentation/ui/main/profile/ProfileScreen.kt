package xyz.mirage.app.presentation.ui.main.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.core.theme.PrimaryColor
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.main.account.detail.components.UserAvatar
import xyz.mirage.app.presentation.ui.main.account.detail.components.UserBanner
import xyz.mirage.app.presentation.ui.main.home.list.components.PostListItem
import xyz.mirage.app.presentation.ui.main.profile.ProfileEvent.*
import xyz.mirage.app.presentation.ui.main.profile.components.*
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.SetDeletePostInList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.SetRefreshPostInList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import xyz.mirage.app.presentation.ui.shared.CircularIndeterminateProgressBar
import xyz.mirage.app.presentation.ui.shared.EmptyPlaceholder
import xyz.mirage.app.presentation.ui.shared.NavBar

@ExperimentalCoilApi
@Composable
fun ProfileScreen(
    username: String?,
    state: ProfileState,
    onTriggerEvent: (ProfileEvent) -> Unit,
    navController: NavController,
    isDarkTheme: Boolean,
    isNetworkAvailable: Boolean,
    scaffoldState: ScaffoldState,
    refreshViewManager: RefreshViewManager,
    authId: String,
    imageLoader: ImageLoader,
) {
    if (state.profile == null && !state.isLoading) {
        username?.let {
            onTriggerEvent(GetProfileEvent(username = it))
        }
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

    val scrollState = rememberLazyListState()

    AppTheme(
        isNetworkAvailable = isNetworkAvailable,
        displayProgressBar = state.isLoading,
        scaffoldState = scaffoldState,
        darkTheme = isDarkTheme,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(OnRemoveHeadFromQueue)
        },
        dialogQueue = state.queue
    ) {
        Scaffold(
            topBar = {
                ProfileAppBar(
                    username = username,
                    handleBack = { navController.popBackStack() },
                    scrollState = scrollState,
                )
            },
            bottomBar = {
                NavBar(navController = navController)
            }
        ) {
            state.profile?.let { user ->
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = state.isRefreshing),
                    onRefresh = { onTriggerEvent(OnChangeTabEvent(state.currentTab)) }
                ) {
                    LazyColumn(
                        modifier = Modifier.offset(0.dp, (-60).dp),
                        state = scrollState
                    ) {
                        item {
                            Box {
                                user.banner?.let { url ->
                                    UserBanner(
                                        url = url,
                                        username = user.username,
                                        imageLoader = imageLoader
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .background(PrimaryColor)
                                        .height(150.dp)
                                        .fillMaxWidth(),
                                )

                                UserAvatar(
                                    url = user.image,
                                    modifier = Modifier.align(Alignment.BottomStart),
                                    isDarkTheme = isDarkTheme,
                                    imageLoader = imageLoader
                                )

                                when {

                                    user.id == authId -> {
                                    }

                                    user.following -> {
                                        Button(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset((-20).dp, 50.dp),
                                            onClick = { onTriggerEvent(OnToggleFollow) },
                                            shape = RoundedCornerShape(20.dp),
                                        ) {
                                            Text(text = "Following", color = Color.White)
                                        }
                                    }

                                    else -> {
                                        OutlinedButton(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset((-20).dp, 50.dp),
                                            onClick = { onTriggerEvent(OnToggleFollow) },
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(1.dp, PrimaryColor)
                                        ) {
                                            Text(text = "Follow", color = PrimaryColor)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.size(60.dp))
                            DisplayName(displayName = user.displayName)
                            Username(username = user.username)

                            user.bio?.let { bio ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Biography(bio = bio)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            FollowerInfo(user = user)
                            Spacer(modifier = Modifier.height(8.dp))
                            JoinDate(date = user.createdAt)
                            Spacer(modifier = Modifier.size(10.dp))
                        }

                        item {
                            PostsTabs(
                                currentTab = state.currentTab,
                                onChangeTab = { index ->
                                    onTriggerEvent(
                                        OnChangeTabEvent(
                                            index,
                                        )
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
                                                val route =
                                                    Screen.PostDetail.route + "/${post.id}"
                                                navController.navigate(route)
                                            },
                                            onNavigateToProfileScreen = {
                                                val route =
                                                    Screen.Profile.route + "/${post.profile.username}"
                                                navController.navigate(route)
                                            },
                                            onToggleLike = {
                                                onTriggerEvent(
                                                    ToggleLikeEvent(
                                                        post.id
                                                    )
                                                )
                                            },
                                            onToggleRetweet = {
                                                onTriggerEvent(
                                                    ToggleRetweetEvent(post.id)
                                                )
                                            },
                                            authId = authId,
                                            onToggleFollow = { onTriggerEvent(OnToggleFollow) },
                                            onToggleDelete = {
                                                onTriggerEvent(
                                                    DeletePostEvent(it)
                                                )
                                            },
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
        }
    }
}