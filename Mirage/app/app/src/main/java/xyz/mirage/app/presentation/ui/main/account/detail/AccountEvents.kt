package xyz.mirage.app.presentation.ui.main.account.detail

import xyz.mirage.app.business.domain.models.Post

sealed class AccountEvents {

    object GetAccount : AccountEvents()

    data class OnChangeTabEvent(
        val tab: Int
    ) : AccountEvents()

    data class OnChangePostScrollPositionEvent(
        val index: Int
    ) : AccountEvents()

    data class UpdateListEvent(val post: Post) : AccountEvents()

    data class RemoveItemEvent(val id: String) : AccountEvents()

    data class ToggleLikeEvent(val id: String) : AccountEvents()

    data class ToggleRetweetEvent(val id: String) : AccountEvents()

    data class DeletePostEvent(val id: String) : AccountEvents()

    data class ToggleFollowEvent(val username: String) : AccountEvents()

    object NextPageEvent : AccountEvents()

    object OnRemoveHeadFromQueue : AccountEvents()

    object OnLogout : AccountEvents()
}