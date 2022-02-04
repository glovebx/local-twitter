package xyz.mirage.app.presentation.ui.main.refresh

import xyz.mirage.app.business.domain.models.Post

sealed class RefreshViewEvents {
    data class SetRefreshAccount(val refresh: Boolean) : RefreshViewEvents()

    data class SetRefreshPostInList(val post: Post?) : RefreshViewEvents()

    data class SetDeletePostInList(val id: String?) : RefreshViewEvents()

    data class AddPostToList(val post: Post?) : RefreshViewEvents()
}