package xyz.mirage.app.presentation.ui.auth.login

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.core.TAG
import xyz.mirage.app.business.domain.core.doesMessageAlreadyExistInQueue
import xyz.mirage.app.business.interactors.auth.Login
import xyz.mirage.app.presentation.session.SessionEvents
import xyz.mirage.app.presentation.session.SessionManager
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val login: Login,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val state: MutableState<LoginState> = mutableStateOf(LoginState())

    fun onTriggerEvent(event: LoginEvents) {
        viewModelScope.launch {
            try {
                when (event) {
                    is LoginEvents.Login -> {
                        executeLogin()
                    }

                    is LoginEvents.OnRemoveHeadFromQueue -> {
                        removeHeadMessage()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "launchJob: Exception: ${e}, ${e.cause}")
                e.printStackTrace()
            }
        }
    }

    private fun executeLogin() {
        state.value.let { state ->
            login.execute(
                email = state.email.text,
                password = state.password.text,
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

    private fun appendToMessageQueue(stateMessage: StateMessage) {
        state.value.let { state ->
            val queue = state.queue
            if (!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)) {
                queue.add(stateMessage)
                this.state.value = state.copy(queue = queue)
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

    private fun resetFields() {
        state.value = LoginState()
    }
}
