package xyz.mirage.app.presentation.ui.main.account.update

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.core.TAG
import xyz.mirage.app.business.domain.core.doesMessageAlreadyExistInQueue
import xyz.mirage.app.business.interactors.account.GetAccountFromCache
import xyz.mirage.app.business.interactors.account.UpdateAccount
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewEvents
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateAccountViewModel
@Inject
constructor(
    private val updateAccount: UpdateAccount,
    private val getAccountFromCache: GetAccountFromCache,
    private val refreshViewManager: RefreshViewManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val state: MutableState<UpdateAccountState> = mutableStateOf(UpdateAccountState())

    init {
        savedStateHandle.get<String>("accountId")?.let { accountPk ->
            onTriggerEvent(UpdateAccountEvents.GetAccountFromCache(accountPk))
        }

    }

    fun onTriggerEvent(event: UpdateAccountEvents) {
        when (event) {

            is UpdateAccountEvents.GetAccountFromCache -> {
                getAccount(event.id)
            }

            is UpdateAccountEvents.UpdatePressed -> {
                handleUpdate()
            }

            is UpdateAccountEvents.OnUpdateBanner -> {
                onUpdateBanner(event.uri)
            }

            is UpdateAccountEvents.OnUpdateImage -> {
                onUpdateImage(event.uri)
            }

            is UpdateAccountEvents.OnRemoveHeadFromQueue -> {
                removeHeadFromQueue()
            }

            UpdateAccountEvents.ToggleOnLoad -> {
                state.value = state.value.copy(onLoad = true)
            }

            is UpdateAccountEvents.OnMessageReceived -> {
                appendToMessageQueue(event.stateMessage)
            }

            UpdateAccountEvents.SetIsDirty -> {
                setIsDirty(true)
            }
        }
    }

    private fun removeHeadFromQueue() {
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
                queue.add(stateMessage)
                this.state.value = state.copy(queue = KQueue(mutableListOf())) // force recompose
                this.state.value = state.copy(queue = queue)
            }
        }
    }

    private fun onUpdateImage(uri: Uri) {
        state.value = state.value.copy(imageURI = uri)
        setIsDirty(true)
    }

    private fun onUpdateBanner(uri: Uri) {
        state.value = state.value.copy(bannerURI = uri)
        setIsDirty(true)
    }

    private fun getAccount(id: String) {
        state.value.let { state ->
            getAccountFromCache.execute(
                id = id,
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let { account ->
                    this.state.value = state.copy(
                        account = account,
                        isLoading = false,
                    )

                    this.state.value.apply {
                        username.text = account.username
                        email.text = account.email
                        displayName.text = account.displayName
                        bio.text = account.bio ?: ""
                    }

                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }

            }.launchIn(viewModelScope)
        }
    }

    private fun handleUpdate() {
        state.value.let { state ->

            setIsDirty(false)

            val email = state.email.text.toRequestBody("text/plain".toMediaTypeOrNull())
            val username = state.username.text.toRequestBody("text/plain".toMediaTypeOrNull())
            val displayName = state.displayName.text.toRequestBody("text/plain".toMediaTypeOrNull())
            val bio = state.bio.text.toRequestBody("text/plain".toMediaTypeOrNull())

            var multipartImage: MultipartBody.Part? = null
            state.imageURI?.path?.let { filePath ->
                val imageFile = File(filePath)
                val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                multipartImage = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    requestBody
                )
            }

            var multipartBanner: MultipartBody.Part? = null
            state.bannerURI?.path?.let { filePath ->
                val imageFile = File(filePath)
                val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                multipartBanner = MultipartBody.Part.createFormData(
                    "banner",
                    imageFile.name,
                    requestBody
                )
            }

            updateAccount.execute(
                email = email,
                username = username,
                displayName = displayName,
                bio = bio,
                image = multipartImage,
                banner = multipartBanner,
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let { _ ->
                    setIsDirty(false)
                    refreshViewManager.onTriggerEvent(RefreshViewEvents.SetRefreshAccount(true))
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }

            }.launchIn(viewModelScope)
        }
    }

    private fun setIsDirty(isDirty: Boolean) {
        state.value = state.value.copy(isDirty = isDirty)
    }
}