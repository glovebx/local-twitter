package xyz.mirage.app.presentation.ui.main.search

import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Post

sealed class SearchEvents {

    object NewSearch : SearchEvents()

    data class UpdateQuery(val query: String) : SearchEvents()

    data class OnUpdateScrollPosition(val position: Int) : SearchEvents()

    data class Error(val stateMessage: StateMessage) : SearchEvents()

    object NextPageEvent : SearchEvents()

    data class OnToggleFollow(val username: String) : SearchEvents()

    data class ToggleLikeEvent(val id: String) : SearchEvents()

    data class ToggleRetweetEvent(val id: String) : SearchEvents()

    data class RemoveItemEvent(val id: String) : SearchEvents()

    data class DeletePostEvent(val id: String) : SearchEvents()

    data class UpdateListEvent(val post: Post) : SearchEvents()

    object OnRemoveHeadFromQueue : SearchEvents()
}