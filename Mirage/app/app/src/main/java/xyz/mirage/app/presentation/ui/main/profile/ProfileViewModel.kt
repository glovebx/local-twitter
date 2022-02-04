package xyz.mirage.app.presentation.ui.main.profile

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
import xyz.mirage.app.business.interactors.post.DeletePost
import xyz.mirage.app.business.interactors.post.ToggleLike
import xyz.mirage.app.business.interactors.post.ToggleRetweet
import xyz.mirage.app.business.interactors.profile.*
import xyz.mirage.app.presentation.ui.main.profile.ProfileEvent.*
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val STATE_KEY_PROFILE = "profile.state.username.key"

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val getProfile: GetProfile,
    private val getProfilePosts: GetProfilePosts,
    private val getProfileLikes: GetProfileLikes,
    private val getProfileMedia: GetProfileMedia,
    private val toggleLike: ToggleLike,
    private val toggleRetweet: ToggleRetweet,
    private val toggleFollow: ToggleFollow,
    private val deletePost: DeletePost,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val state: MutableState<ProfileState> = mutableStateOf(ProfileState())

    init {
        // restore if process dies
        savedStateHandle.get<String>(STATE_KEY_PROFILE)?.let { username ->
            onTriggerEvent(GetProfileEvent(username))
        }
    }

    fun onTriggerEvent(event: ProfileEvent) {
        viewModelScope.launch {
            try {
                when (event) {
                    is GetProfileEvent -> {
                        if (state.value.profile == null) {
                            getProfile(event.username)
                        }
                    }

                    is NextPageEvent -> {
                        nextPostsByTab()
                    }

                    OnRemoveHeadFromQueue -> {
                        removeHeadMessage()
                    }

                    is OnChangePostScrollPositionEvent -> {
                        onChangePostScrollPosition(event.index)
                    }

                    is OnChangeTabEvent -> {
                        onChangeTab(event.tab)
                    }

                    OnToggleFollow -> {
                        onToggleFollow()
                    }

                    is ToggleLikeEvent -> {
                        onToggleLike(event.id)
                    }

                    is ToggleRetweetEvent -> {
                        onToggleRetweet(event.id)
                    }

                    is DeletePostEvent -> {
                        confirmDeletePost(event.id)
                    }

                    is UpdateListEvent -> {
                        updateList(event.post)
                    }

                    is RemoveItemEvent -> {
                        removeItem(event.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getProfile(username: String) {
        getProfile.execute(
            username = username,
        ).onEach { dataState ->
            state.value = state.value.copy(isLoading = dataState.isLoading)

            dataState.data?.let { data ->
                state.value = state.value.copy(profile = data)
                savedStateHandle.set(STATE_KEY_PROFILE, data.username)
                getProfilePostsByTab()
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun onChangeTab(tab: Int) {
        state.value = state.value.copy(currentTab = tab)
        getProfilePostsByTab()
    }

    private fun getProfilePostsByTab() {
        resetState()

        when (state.value.currentTab) {
            0 -> getProfilePosts
            1 -> getProfileMedia
            else -> getProfileLikes
        }.execute(
            username = state.value.profile?.username ?: throw Exception("No profile"),
            cursor = null,
        ).onEach { dataState ->
            state.value = state.value.copy(postsIsLoading = dataState.isLoading)

            dataState.data?.let { list ->
                state.value = state.value.copy(posts = list)
            }

            dataState.stateMessage?.let { stateMessage ->
                appendToMessageQueue(stateMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun nextPostsByTab() {
        val posts = state.value.posts

        if ((state.value.postListScrollPosition + 1) >= (state.value.page * PAGE_SIZE)) {
            incrementPage()

            if (state.value.page > 1) {
                when (state.value.currentTab) {
                    0 -> getProfilePosts
                    1 -> getProfileMedia
                    else -> getProfileLikes
                }.execute(
                    username = state.value.profile?.username ?: throw Exception("No profile"),
                    cursor = posts[posts.size - 1].createdAt,
                ).onEach { dataState ->
                    state.value = state.value.copy(postsIsLoading = dataState.isLoading)

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
        state.value = state.value.copy(postListScrollPosition = position)
    }

    /**
     * Called when a new search is executed.
     */
    private fun resetState() {
        state.value = state.value.copy(
            posts = emptyList(),
            page = 1
        )
        onChangePostScrollPosition(0)
    }

    private fun appendToMessageQueue(stateMessage: StateMessage) {
        state.value.let { state ->
            val queue = state.queue
            if (!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)) {
                queue.add(stateMessage)
                this.state.value = state.copy(queue = queue)
            }
        }
    }

    private fun onToggleFollow() {
        state.value.let { state ->
            state.profile?.let { profile ->
                toggleFollow.execute(
                    username = profile.username
                ).onEach { dataState ->
                    this.state.value = state.copy(isLoading = dataState.isLoading)

                    dataState.data?.let { response ->
                        val current = ArrayList(state.posts)

                        current.find { it.profile.username == response.username }?.apply {
                            this.profile.following = response.following
                        }

                        this.state.value = state.copy(
                            profile = profile.copy(
                                following = response.following,
                                followers = if (response.following) profile.followers + 1 else profile.followers - 1
                            ),
                            posts = current
                        )
                    }

                    dataState.stateMessage?.let { stateMessage ->
                        appendToMessageQueue(stateMessage)
                    }
                }.launchIn(viewModelScope)
            }
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
    }

    private fun removeItem(id: String) {
        removePostFromList(id)
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

        state.value = state.value.copy(
            posts = current,
        )
    }
}