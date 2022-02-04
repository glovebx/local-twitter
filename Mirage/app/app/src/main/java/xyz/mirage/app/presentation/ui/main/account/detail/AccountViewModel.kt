package xyz.mirage.app.presentation.ui.main.account.detail

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
import xyz.mirage.app.business.interactors.account.GetAccount
import xyz.mirage.app.business.interactors.post.DeletePost
import xyz.mirage.app.business.interactors.post.ToggleLike
import xyz.mirage.app.business.interactors.post.ToggleRetweet
import xyz.mirage.app.business.interactors.profile.GetProfileLikes
import xyz.mirage.app.business.interactors.profile.GetProfileMedia
import xyz.mirage.app.business.interactors.profile.GetProfilePosts
import xyz.mirage.app.business.interactors.profile.ToggleFollow
import xyz.mirage.app.presentation.session.SessionEvents
import xyz.mirage.app.presentation.session.SessionManager
import xyz.mirage.app.presentation.ui.main.account.detail.AccountEvents.*
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class AccountViewModel
@Inject
constructor(
    private val getAccount: GetAccount,
    private val getProfilePosts: GetProfilePosts,
    private val getProfileLikes: GetProfileLikes,
    private val getProfileMedia: GetProfileMedia,
    private val toggleLike: ToggleLike,
    private val toggleRetweet: ToggleRetweet,
    private val deletePost: DeletePost,
    private val toggleFollow: ToggleFollow,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val state: MutableState<AccountState> = mutableStateOf(AccountState())

    private var postListScrollPosition = 0

    init {
        fetchAccount()
    }

    fun onTriggerEvent(event: AccountEvents) {
        when (event) {
            is AccountEvents.GetAccount -> {
                fetchAccount()
            }

            is OnRemoveHeadFromQueue -> {
                removeHeadFromQueue()
            }

            is OnChangeTabEvent -> {
                onChangeTab(event.tab)
            }

            NextPageEvent -> {
                nextPostsByTab()
            }

            is OnChangePostScrollPositionEvent -> {
                onChangePostScrollPosition(event.index)
            }

            is OnLogout -> {
                onLogout()
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

            is ToggleFollowEvent -> {
                handleToggleFollow(event.username)
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
                    this.state.value =
                        state.copy(queue = KQueue(mutableListOf())) // force recompose
                    this.state.value = state.copy(queue = queue)
                }
            }
        }
    }

    private fun fetchAccount() {
        state.value.let { state ->
            getAccount.execute().onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let { account ->
                    this.state.value = state.copy(account = account)
                    getProfilePostsByTab()
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }

            }.launchIn(viewModelScope)
        }
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
            username = state.value.account?.username ?: "",
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

    private fun resetState() {
        state.value = state.value.copy(
            posts = emptyList(),
            page = 1
        )
        onChangePostScrollPosition(0)
    }

    private fun onChangePostScrollPosition(position: Int) {
        postListScrollPosition = position
    }

    private fun nextPostsByTab() {
        val posts = state.value.posts

        if ((postListScrollPosition + 1) >= (state.value.page * PAGE_SIZE)) {
            incrementPage()

            if (state.value.page > 1) {
                when (state.value.currentTab) {
                    0 -> getProfilePosts
                    1 -> getProfileMedia
                    else -> getProfileLikes
                }.execute(
                    username = state.value.account?.username ?: "",
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

    private fun incrementPage() {
        state.value = state.value.copy(page = state.value.page + 1)
    }

    private fun appendPosts(list: List<Post>) {
        val current = ArrayList(state.value.posts)
        current.addAll(list)
        state.value = state.value.copy(posts = current)
    }

    private fun onLogout() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                sessionManager.onTriggerEvent(SessionEvents.Logout)
            }

            override fun cancel() {
                state.value = state.value.copy(queue = KQueue(mutableListOf()))
            }
        }

        val message = StateMessage(
            response = Response(
                message = "Are you sure you want to logout?",
                uiComponentType = UIComponentType.AreYouSureDialog(callback),
                messageType = MessageType.Info()
            ),
        )

        appendToMessageQueue(message)
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