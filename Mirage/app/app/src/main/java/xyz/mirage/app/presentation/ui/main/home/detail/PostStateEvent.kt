package xyz.mirage.app.presentation.ui.main.home.detail

sealed class PostEvent {

    data class GetPostEvent(
        val id: String
    ) : PostEvent()

    data class ToggleRetweetEvent(
        val id: String
    ) : PostEvent()

    data class ToggleLikeEvent(
        val id: String
    ) : PostEvent()

    data class ToggleFollowEvent(
        val username: String
    ) : PostEvent()

    data class DeletePostEvent(val id: String) : PostEvent()

    object OnToggleOnLoad : PostEvent()

    object OnRemoveHeadFromQueue : PostEvent()
}