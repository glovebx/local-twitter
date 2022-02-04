package xyz.mirage.app.presentation.ui.main.search

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.interactors.post.DeletePost
import xyz.mirage.app.business.interactors.post.SearchPosts
import xyz.mirage.app.business.interactors.post.ToggleLike
import xyz.mirage.app.business.interactors.post.ToggleRetweet
import xyz.mirage.app.business.interactors.profile.SearchProfiles
import xyz.mirage.app.business.interactors.profile.ToggleFollow
import xyz.mirage.app.presentation.ui.main.search.SearchEvents.*
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class SearchViewModel
@Inject
constructor(
    private val searchProfiles: SearchProfiles,
    private val searchPosts: SearchPosts,
    private val toggleLike: ToggleLike,
    private val toggleRetweet: ToggleRetweet,
    private val toggleFollow: ToggleFollow,
    private val deletePost: DeletePost,
) : ViewModel() {

    val state: MutableState<SearchState> = mutableStateOf(SearchState())

    fun onTriggerEvent(event: SearchEvents) {
        when (event) {
            is NewSearch -> {
                executeSearch()
            }

            is UpdateQuery -> {
                onUpdateQuery(event.query)
            }

            is Error -> {
                appendToMessageQueue(event.stateMessage)
            }

            is OnRemoveHeadFromQueue -> {
                removeHeadFromQueue()
            }

            is OnUpdateScrollPosition -> {
                onChangeProfileScrollPosition(event.position)
            }

            NextPageEvent -> {
                nextPage()
            }

            is DeletePostEvent -> {
                confirmDeletePost(event.id)
            }

            is OnToggleFollow -> {
                handleToggleFollow(event.username)
            }

            is ToggleLikeEvent -> {
                onToggleLike(event.id)
            }

            is ToggleRetweetEvent -> {
                onToggleRetweet(event.id)
            }

            is UpdateListEvent -> {
                updateList(event.post)
            }

            is RemoveItemEvent -> {
                removeItem(event.id)
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

    private fun appendToMessageQueue(stateMessage: StateMessage) {
        state.value.let { state ->
            val queue = state.queue
            if (!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)) {
                if (stateMessage.response.uiComponentType !is UIComponentType.None) {
                    queue.add(stateMessage)
                    this.state.value = state.copy(queue = queue)
                }
            }
        }
    }

    private fun clearList() {
        state.value.let { state ->
            this.state.value = state.copy(
                profiles = listOf(),
                posts = listOf(),
                listPosition = 0,
                page = 0,
            )
        }
    }

    private fun onUpdateQuery(query: String) {
        state.value = state.value.copy(query = query)
    }

    private fun executeSearch() {
        clearList()
        if (state.value.query.startsWith("#")) {
            executePostsSearch()
        } else {
            executeProfileSearch()
        }
    }

    private fun executeProfileSearch() {
        state.value.let { state ->
            searchProfiles.execute(
                search = state.query,
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let { list ->
                    this.state.value = state.copy(profiles = list)
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }

            }.launchIn(viewModelScope)
        }
    }

    private fun executePostsSearch() {
        state.value.let { state ->
            searchPosts.execute(
                search = state.query,
                cursor = null,
                page = state.page,
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let { list ->
                    this.state.value = state.copy(posts = list)
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }

            }.launchIn(viewModelScope)
        }
    }

    private fun onChangeProfileScrollPosition(position: Int) {
        state.value = state.value.copy(listPosition = position)
    }

    private fun incrementPage() {
        state.value = state.value.copy(page = state.value.page + 1)
    }

    private fun nextPage() {
        val posts = state.value.posts

        if ((state.value.listPosition + 1) >= (state.value.page * PAGE_SIZE)) {
            incrementPage()

            if (state.value.page > 1) {
                searchPosts.execute(
                    search = state.value.query,
                    page = state.value.page,
                    cursor = posts[posts.size - 1].createdAt,
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