package xyz.mirage.app.presentation.ui.main.refresh

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshViewManager
@Inject
constructor(
) {
    val state: MutableState<RefreshViewState> = mutableStateOf(RefreshViewState())

    fun onTriggerEvent(event: RefreshViewEvents) {
        when (event) {
            is RefreshViewEvents.SetRefreshAccount -> {
                state.value = state.value.copy(
                    shouldRefreshAccount = event.refresh
                )
            }

            is RefreshViewEvents.SetRefreshPostInList -> {
                state.value = state.value.copy(
                    postToRefresh = event.post
                )
            }

            is RefreshViewEvents.SetDeletePostInList -> {
                state.value = state.value.copy(
                    postToRemove = event.id
                )
            }

            is RefreshViewEvents.AddPostToList -> {
                state.value = state.value.copy(
                    postToAdd = event.post
                )
            }
        }
    }
}