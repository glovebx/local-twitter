package xyz.mirage.app.presentation.ui.auth.register

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.core.TAG
import xyz.mirage.app.business.domain.core.doesMessageAlreadyExistInQueue
import xyz.mirage.app.business.interactors.auth.Register
import xyz.mirage.app.presentation.session.SessionEvents
import xyz.mirage.app.presentation.session.SessionManager
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
@Inject
constructor(
    private val register: Register,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val state: MutableState<RegisterState> = mutableStateOf(RegisterState())

    fun onTriggerEvent(event: RegisterEvents) {
        viewModelScope.launch {
            try {
                when (event) {
                    RegisterEvents.OnRemoveHeadFromQueue -> {
                        removeHeadFromQueue()
                    }

                    RegisterEvents.RegisterClicked -> {
                        executeRegister()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "launchJob: Exception: ${e}, ${e.cause}")
                e.printStackTrace()
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
                queue.add(stateMessage)
                this.state.value = state.copy(queue = queue)
            }
        }
    }

    private fun executeRegister() {
        state.value.let { state ->
            register.execute(
                email = state.email.text,
                username = state.username.text,
                password = state.password.text,
                displayName = state.displayName.text,
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let { account ->
                    sessionManager.onTriggerEvent(SessionEvents.Login(account))
                    resetFields()
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun resetFields() {
        state.value = RegisterState()
    }
}
