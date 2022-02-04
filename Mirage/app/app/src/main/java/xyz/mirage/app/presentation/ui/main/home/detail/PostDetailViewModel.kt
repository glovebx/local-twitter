package xyz.mirage.app.presentation.ui.main.home.detail

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
import xyz.mirage.app.business.interactors.post.DeletePost
import xyz.mirage.app.business.interactors.post.GetPost
import xyz.mirage.app.business.interactors.post.ToggleLike
import xyz.mirage.app.business.interactors.post.ToggleRetweet
import xyz.mirage.app.business.interactors.profile.ToggleFollow
import xyz.mirage.app.presentation.core.util.ConnectivityManager
import xyz.mirage.app.presentation.ui.main.home.detail.PostEvent.*
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.SetDeletePostInList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.SetRefreshPostInList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import javax.inject.Inject

private const val STATE_KEY_POST = "post.state.post.key"

@HiltViewModel
class PostDetailViewModel
@Inject
constructor(
    private val getPost: GetPost,
    private val toggleLike: ToggleLike,
    private val toggleRetweet: ToggleRetweet,
    private val toggleFollow: ToggleFollow,
    private val deletePost: DeletePost,
    private val connectivityManager: ConnectivityManager,
    private val refreshViewManager: RefreshViewManager,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val state: MutableState<PostDetailState> = mutableStateOf(PostDetailState())

    init {
        // restore if process dies
        savedStateHandle.get<String>(STATE_KEY_POST)?.let { postId ->
            onTriggerEvent(GetPostEvent(postId))
        }
    }

    fun onTriggerEvent(event: PostEvent) {
        viewModelScope.launch {
            try {
                when (event) {

                    is GetPostEvent -> {
                        if (state.value.post == null) {
                            getPost(event.id)
                        }
                    }

                    OnRemoveHeadFromQueue -> {
                        removeHeadMessage()
                    }

                    OnToggleOnLoad -> {
                        state.value = state.value.copy(onLoad = true)
                    }

                    is DeletePostEvent -> {
                        confirmDeletePost(event.id)
                    }

                    is ToggleFollowEvent -> {
                        handleToggleFollow(event.username)
                    }

                    is ToggleLikeEvent -> {
                        handleToggleLike(event.id)
                    }

                    is ToggleRetweetEvent -> {
                        handleToggleRetweet(event.id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "launchJob: Exception: ${e}, ${e.cause}")
                e.printStackTrace()
            }
        }
    }

    private fun getPost(id: String) {
        getPost.execute(
            id = id,
            isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
        ).onEach { dataState ->
            state.value = state.value.copy(isLoading = dataState.isLoading)

            dataState.data?.let { data ->
                state.value = state.value.copy(post = data)
                savedStateHandle.set(STATE_KEY_POST, data.id)
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun appendToMessageQueue(stateMessage: StateMessage) {
        if (!stateMessage.doesMessageAlreadyExistInQueue(queue = state.value.queue)) {
            val queue = state.value.queue
            queue.add(stateMessage)
            state.value = state.value.copy(queue = KQueue(mutableListOf())) // force recompose
            state.value = state.value.copy(queue = queue)
        }
    }

    private fun removeHeadMessage() {
        try {
            val queue = state.value.queue
            queue.remove() // can throw exception if empty
            state.value = state.value.copy(queue = KQueue(mutableListOf())) // force recompose
            state.value = state.value.copy(queue = queue)
        } catch (e: Exception) {
            Log.d(TAG, "Nothing to remove from DialogQueue")
        }
    }

    private fun handleToggleLike(id: String) {
        toggleLike.execute(
            id = id,
        ).onEach { dataState ->
            dataState.data?.let { data ->
                state.value.post?.let { post ->
                    state.value = state.value.copy(
                        post = post.copy(
                            liked = data.liked,
                            likes = if (data.liked) post.likes + 1 else post.likes - 1
                        )
                    )

                    refreshViewManager.onTriggerEvent(SetRefreshPostInList(post = state.value.post))
                }
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleToggleRetweet(id: String) {
        toggleRetweet.execute(
            id = id,
        ).onEach { dataState ->
            dataState.data?.let { data ->
                state.value.post?.let { post ->
                    state.value = state.value.copy(
                        post = post.copy(
                            retweeted = data.retweeted,
                            retweets = if (data.retweeted) post.retweets + 1 else post.retweets - 1
                        )
                    )
                }

                refreshViewManager.onTriggerEvent(SetRefreshPostInList(post = state.value.post))
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
            id = id
        ).onEach { dataState ->

            dataState.data?.let { _ ->
                refreshViewManager.onTriggerEvent(SetDeletePostInList(id = id))
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleToggleFollow(username: String) {
        toggleFollow.execute(username = username).onEach { dataState ->
            dataState.data?.let { profile ->

                state.value.post?.let { post ->
                    state.value = state.value.copy(
                        post = post.copy(
                            profile = profile.copy(
                                following = post.profile.following
                            )
                        )
                    )
                }
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }
}
