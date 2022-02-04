package xyz.mirage.app.presentation.ui.main.home.list

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.interactors.post.*
import xyz.mirage.app.business.interactors.profile.ToggleFollow
import xyz.mirage.app.presentation.core.util.ConnectivityManager
import xyz.mirage.app.presentation.ui.main.home.list.PostListEvent.*
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val STATE_KEY_PAGE = "post.state.page.key"
private const val STATE_KEY_LIST_POSITION = "post.state.query.list_position"

@HiltViewModel
class PostListViewModel
@Inject
constructor(
    private val getFeed: GetFeed,
    private val toggleLike: ToggleLike,
    private val toggleRetweet: ToggleRetweet,
    private val toggleFollow: ToggleFollow,
    private val deletePost: DeletePost,
    private val restorePosts: RestorePosts,
    private val connectivityManager: ConnectivityManager,
    private val savedStateHandle: SavedStateHandle,
    private val refreshViewManager: RefreshViewManager,
) : ViewModel() {

    val state: MutableState<PostListState> = mutableStateOf(PostListState())

    private var postListScrollPosition = 0

    init {
        savedStateHandle.get<Int>(STATE_KEY_PAGE)?.let { p ->
            setPage(p)
        }
        savedStateHandle.get<Int>(STATE_KEY_LIST_POSITION)?.let { p ->
            setListScrollPosition(p)
        }

        // Were they doing something before the process died?
        if (postListScrollPosition != 0) {
            onTriggerEvent(RestoreStateEvent)
        } else {
            onTriggerEvent(FeedEvent)
        }
    }

    fun onTriggerEvent(event: PostListEvent) {
        viewModelScope.launch {
            try {
                when (event) {
                    is FeedEvent -> {
                        getFeed()
                    }

                    is NextPageEvent -> {
                        nextPage()
                    }

                    is RestoreStateEvent -> {
                        restoreState()
                    }

                    OnRemoveHeadFromQueue -> {
                        removeHeadFromQueue()
                    }

                    is ToggleLikeEvent -> {
                        onToggleLike(event.id)
                    }

                    is ToggleRetweetEvent -> {
                        onToggleRetweet(event.id)
                    }

                    is ChangeScrollPositionEvent -> {
                        onChangePostScrollPosition(event.position)
                    }

                    is DeletePostEvent -> {
                        confirmDeletePost(event.id)
                    }

                    is ToggleFollowEvent -> {
                        handleToggleFollow(event.username)
                    }

                    is UpdateListEvent -> {
                        updateList(event.post)
                    }

                    is RemoveItemEvent -> {
                        removeItem(event.id)
                    }

                    is AddPostToListEvent -> {
                        addPostToList(event.post)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
            }
        }
    }

    private fun getFeed() {
        resetState()

        getFeed.execute(
            page = state.value.page,
            cursor = null,
            isNetworkAvailable = connectivityManager.isNetworkAvailable.value
        ).onEach { dataState ->
            state.value = state.value.copy(isLoading = dataState.isLoading)

            dataState.data?.let { list ->
                state.value = state.value.copy(posts = list, deletedCount = 0)
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)

    }

    private fun restoreState() {
        restorePosts.execute(page = state.value.page).onEach { dataState ->
            state.value = state.value.copy(isLoading = dataState.isLoading)

            dataState.data?.let { list ->
                state.value = state.value.copy(posts = list, deletedCount = 0)
            }

            dataState.stateMessage?.let { error ->
                appendToMessageQueue(error)
            }
        }.launchIn(viewModelScope)
    }

    private fun nextPage() {
        val posts = state.value.posts

        if ((postListScrollPosition + 1) >= (state.value.page * PAGE_SIZE - state.value.deletedCount)) {
            incrementPage()
            Log.d(TAG, "nextPage: ${posts[posts.size - 1].createdAt}")

            if (state.value.page > 1) {
                getFeed.execute(
                    page = state.value.page,
                    cursor = posts[posts.size - 1].createdAt,
                    isNetworkAvailable = connectivityManager.isNetworkAvailable.value
                ).onEach { dataState ->
                    state.value = state.value.copy(isLoading = dataState.isLoading)

                    dataState.data?.let { list ->
                        appendPosts(list)
                    }

                    dataState.stateMessage?.let { error ->
                        appendToMessageQueue(error)
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    /**
     * Append new posts to the current list of posts
     */
    private fun appendPosts(list: List<Post>) {
        val current = ArrayList(state.value.posts)
        current.addAll(list)
        state.value = state.value.copy(posts = current)
    }

    private fun incrementPage() {
        state.value = state.value.copy(page = state.value.page + 1)
    }

    private fun onChangePostScrollPosition(position: Int) {
        postListScrollPosition = position
    }

    /**
     * Called when a new search is executed.
     */
    private fun resetState() {
        state.value = state.value.copy(
            posts = listOf(),
            page = 1,
            deletedCount = 0
        )
        onChangePostScrollPosition(0)
    }

    private fun setListScrollPosition(position: Int) {
        postListScrollPosition = position
        savedStateHandle.set(STATE_KEY_LIST_POSITION, position)
    }

    private fun setPage(page: Int) {
        state.value = state.value.copy(page = page)
        savedStateHandle.set(STATE_KEY_PAGE, page)
    }

    private fun appendToMessageQueue(stateMessage: StateMessage) {
        state.value.let { state ->
            val queue = state.queue
            if (!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)) {
                if (stateMessage.response.uiComponentType !is UIComponentType.None) {
                    queue.add(stateMessage)
                    this.state.value =
                        state.copy(queue = KQueue(mutableListOf())) // force recompose
                    this.state.value = state.copy(queue = queue)
                }
            }
        }
    }

    private fun removeHeadFromQueue() {
        state.value.let { state ->
            try {
                val queue = state.queue
                queue.remove() // can throw exception if empty
                this.state.value = state.copy(queue = queue)
            } catch (e: Exception) {
                Log.d(TAG, "removeHeadFromQueue: Nothing to remove from DialogQueue")
            }
        }
    }

    private fun onToggleLike(id: String) {
        toggleLike.execute(
            id
        ).onEach { dataState ->
            dataState.data?.let { response ->
                val current = ArrayList(state.value.posts)

                // Force recompose
                state.value = state.value.copy(
                    posts = listOf(),
                )

                current.find { it.id == id }?.apply {
                    this.likes = if (response.liked) this.likes + 1 else this.likes - 1
                    this.liked = response.liked
                }

                state.value = state.value.copy(
                    posts = current,
                )
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun onToggleRetweet(id: String) {

        toggleRetweet.execute(
            id
        ).onEach { dataState ->
            dataState.data?.let { response ->
                val current = ArrayList(state.value.posts)

                // Force recompose
                state.value = state.value.copy(
                    posts = listOf(),
                )

                current.find { it.id == id }?.apply {
                    this.retweets = if (response.retweeted) this.retweets + 1 else this.retweets - 1
                    this.retweeted = response.retweeted
                }

                state.value = state.value.copy(
                    posts = current,
                )
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun confirmDeletePost(id: String) {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                handleDeletePost(id)
            }

            override fun cancel() {
                state.value = state.value.copy(queue = KQueue(mutableListOf()))
            }
        }

        val message = StateMessage(
            response = Response(
                message = "Are you sure you want to delete this post?",
                uiComponentType = UIComponentType.AreYouSureDialog(callback),
                messageType = MessageType.Info()
            ),
        )

        appendToMessageQueue(message)
    }

    private fun handleDeletePost(id: String) {
        deletePost.execute(
            id
        ).onEach { dataState ->
            dataState.data?.let { _ ->
                removePostFromList(id)
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleToggleFollow(username: String) {
        toggleFollow.execute(username = username).onEach { dataState ->
            dataState.data?.let { profile ->
                val current = ArrayList(state.value.posts)

                // Force recompose
                state.value = state.value.copy(
                    posts = listOf(),
                )

                current.find { it.profile.username == username }?.apply {
                    this.profile.following = profile.following
                }

                state.value = state.value.copy(
                    posts = current,
                )
            }


            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun updateList(post: Post) {
        val current = ArrayList(state.value.posts)

        // Force recompose
        state.value = state.value.copy(
            posts = listOf(),
        )

        val list = current.map { if (it.id == post.id) post else it }

        state.value = state.value.copy(
            posts = list,
        )

        refreshViewManager.onTriggerEvent(RefreshViewEvents.SetRefreshPostInList(null))
    }

    private fun addPostToList(post: Post) {
        val current = ArrayList(state.value.posts)

        // Force recompose
        state.value = state.value.copy(
            posts = listOf(),
        )

        current.add(0, post)

        state.value = state.value.copy(
            posts = current,
        )

        refreshViewManager.onTriggerEvent(RefreshViewEvents.AddPostToList(null))
    }

    private fun removeItem(id: String) {
        removePostFromList(id)
        refreshViewManager.onTriggerEvent(RefreshViewEvents.SetDeletePostInList(null))
    }

    private fun removePostFromList(id: String) {
        val current = ArrayList(state.value.posts)

        // Force recompose
        state.value = state.value.copy(
            posts = listOf(),
        )

        current.find { it.id == id }?.let {
            current.remove(it)
        }

        val deletedCount = state.value.deletedCount
        state.value = state.value.copy(
            posts = current,
            deletedCount = deletedCount + 1,
        )
    }

}