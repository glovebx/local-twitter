package xyz.mirage.app.presentation.ui.main.home.list

import xyz.mirage.app.business.domain.models.Post

sealed class PostListEvent {

    object FeedEvent : PostListEvent()

    object NextPageEvent : PostListEvent()

    data class UpdateListEvent(val post: Post) : PostListEvent()

    data class AddPostToListEvent(val post: Post) : PostListEvent()

    data class ToggleLikeEvent(val id: String) : PostListEvent()

    data class ToggleRetweetEvent(val id: String) : PostListEvent()

    data class ToggleFollowEvent(val username: String) : PostListEvent()

    data class DeletePostEvent(val id: String) : PostListEvent()

    data class RemoveItemEvent(val id: String) : PostListEvent()

    data class ChangeScrollPositionEvent(val position: Int) : PostListEvent()

    // restore after process death
    object RestoreStateEvent : PostListEvent()

    object OnRemoveHeadFromQueue : PostListEvent()
}