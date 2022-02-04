package xyz.mirage.app.presentation.ui.main.profile

import xyz.mirage.app.business.domain.models.Post

sealed class ProfileEvent {

    data class OnChangePostScrollPositionEvent(
        val index: Int
    ) : ProfileEvent()

    data class OnChangeTabEvent(
        val tab: Int,
    ) : ProfileEvent()

    data class GetProfileEvent(
        val username: String
    ) : ProfileEvent()

    object NextPageEvent : ProfileEvent()

    object OnToggleFollow : ProfileEvent()

    data class ToggleLikeEvent(val id: String) : ProfileEvent()

    data class ToggleRetweetEvent(val id: String) : ProfileEvent()

    data class DeletePostEvent(val id: String) : ProfileEvent()

    data class RemoveItemEvent(val id: String) : ProfileEvent()

    data class UpdateListEvent(val post: Post) : ProfileEvent()

    object OnRemoveHeadFromQueue : ProfileEvent()
}