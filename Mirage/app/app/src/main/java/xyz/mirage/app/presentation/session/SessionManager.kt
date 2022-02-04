package xyz.mirage.app.presentation.session

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.mirage.app.business.datasources.cache.AppDatabase
import xyz.mirage.app.business.datasources.datastore.AppDataStore
import xyz.mirage.app.business.domain.core.DataStoreKeys
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.core.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import xyz.mirage.app.business.domain.core.SuccessHandling.Companion.SUCCESS_LOGOUT
import xyz.mirage.app.business.domain.core.TAG
import xyz.mirage.app.business.domain.core.doesMessageAlreadyExistInQueue
import xyz.mirage.app.business.domain.models.Account
import xyz.mirage.app.business.interactors.session.CheckPreviousAuthUser
import xyz.mirage.app.business.interactors.session.Logout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    private val checkPreviousAuthUser: CheckPreviousAuthUser,
    private val logout: Logout,
    private val appDataStoreManager: AppDataStore,
    private val cookieJar: ClearableCookieJar,
    private val appDatabase: AppDatabase,
) {

    private val sessionScope = CoroutineScope(Main)

    val state: MutableLiveData<SessionState> = MutableLiveData(SessionState())

    init {
        // Check if a user was authenticated in a previous session
        sessionScope.launch {
            appDataStoreManager.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)?.let { username ->
                onTriggerEvent(SessionEvents.CheckPreviousAuthUser(username))
            } ?: onFinishCheckingPrevAuthUser()
        }
    }

    fun onTriggerEvent(event: SessionEvents) {
        when (event) {
            is SessionEvents.Login -> {
                login(event.account)
            }
            is SessionEvents.Logout -> {
                logout()
            }
            is SessionEvents.CheckPreviousAuthUser -> {
                checkPreviousAuthUser(username = event.username)
            }
            is SessionEvents.OnRemoveHeadFromQueue -> {
                removeHeadFromQueue()
            }
        }
    }

    private fun removeHeadFromQueue() {
        state.value?.let { state ->
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
        state.value?.let { state ->
            val queue = state.queue
            if (!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)) {
                queue.add(stateMessage)
                this.state.value = state.copy(queue = queue)
            }
        }
    }

    private fun checkPreviousAuthUser(username: String) {
        state.value?.let { state ->
            checkPreviousAuthUser.execute(username).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)
                dataState.data?.let { account ->
                    onTriggerEvent(SessionEvents.Login(account))
                }

                dataState.stateMessage?.let { stateMessage ->
                    if (stateMessage.response.message.equals(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE)) {
                        onFinishCheckingPrevAuthUser()
                    } else {
                        appendToMessageQueue(stateMessage)
                    }
                }
            }.launchIn(sessionScope)
        }
    }

    private fun login(account: Account) {
        state.value?.let { state ->
            this.state.value = state.copy(uid = account.id)
        }
    }

    private fun logout() {
        state.value?.let { state ->
            logout.execute().onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)
                dataState.data?.let { response ->
                    if (response.message.equals(SUCCESS_LOGOUT)) {
                        this.state.value = state.copy(uid = null)
                        cookieJar.clearSession()
                        clearAuthUser()
                        clearDatabase()
                        onFinishCheckingPrevAuthUser()
                    }
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }
            }.launchIn(sessionScope)
        }
    }

    private fun onFinishCheckingPrevAuthUser() {
        state.value?.let { state ->
            this.state.value = state.copy(didCheckForPreviousAuthUser = true)
        }
    }

    private fun clearAuthUser() {
        sessionScope.launch {
            appDataStoreManager.setValue(DataStoreKeys.PREVIOUS_AUTH_USER, "")
        }
    }

    private fun clearDatabase() {
        GlobalScope.launch {
            appDatabase.clearAllTables()
        }
    }
}
