package xyz.mirage.app.presentation.ui.main.home.create

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.interactors.account.GetAccountFromCache
import xyz.mirage.app.business.interactors.post.CreatePost
import xyz.mirage.app.presentation.session.SessionManager
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostEvents.*
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents.AddPostToList
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val createPost: CreatePost,
    private val getAccountFromCache: GetAccountFromCache,
    private val refreshViewManager: RefreshViewManager
) : ViewModel() {

    val state: MutableState<CreatePostState> = mutableStateOf(CreatePostState())

    init {
        getAccount()
    }

    fun onTriggerEvent(event: CreatePostEvents) {
        when (event) {

            is OnUpdateUri -> {
                onUpdateUri(event.uri)
            }

            is PublishPost -> {
                publishPost()
            }

            is OnMessageReceived -> {
                appendToMessageQueue(event.stateMessage)
            }

            is OnRemoveHeadFromQueue -> {
                removeHeadMessage()
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

    private fun onPublishSuccess() {
        state.value.let { state ->
            this.state.value = state.copy(onPublishSuccess = true)
        }
    }

    private fun onUpdateUri(uri: Uri?) {
        state.value.let { state ->
            this.state.value = state.copy(uri = uri)
        }
    }

    private fun publishPost() {
        state.value.let { state ->
            val text = state.text.text.toRequestBody("text/plain".toMediaTypeOrNull())

            if (state.text.text.isEmpty() && state.uri == null) {
                onTriggerEvent(
                    OnMessageReceived(
                        stateMessage = StateMessage(
                            response = Response(
                                message = ErrorHandling.ERROR_MUST_SELECT_IMAGE,
                                uiComponentType = UIComponentType.Dialog(),
                                messageType = MessageType.Error()
                            )
                        )
                    )
                )
            } else {
                var multipartBody: MultipartBody.Part? = null
                state.uri?.path?.let { filePath ->
                    val imageFile = File(filePath)
                    val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    multipartBody = MultipartBody.Part.createFormData(
                        "file",
                        imageFile.name,
                        requestBody
                    )
                }

                createPost.execute(
                    text = text,
                    file = multipartBody,
                ).onEach { dataState ->
                    this.state.value = state.copy(isLoading = dataState.isLoading)

                    dataState.data?.let { response ->
                        refreshViewManager.onTriggerEvent(AddPostToList(response))
                        onPublishSuccess()
                    }

                    dataState.stateMessage?.let { stateMessage ->
                        appendToMessageQueue(stateMessage)
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    private fun getAccount() {
        state.value.let { state ->
            getAccountFromCache.execute(
                id = sessionManager.state.value?.uid ?: throw Exception("No UID stored"),
            ).onEach { dataState ->
                dataState.data?.let { account ->
                    this.state.value = state.copy(account = account)
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }

            }.launchIn(viewModelScope)
        }
    }
}
