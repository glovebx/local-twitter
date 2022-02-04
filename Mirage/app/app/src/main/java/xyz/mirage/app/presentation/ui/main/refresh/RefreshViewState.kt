package xyz.mirage.app.presentation.ui.main.refresh

import xyz.mirage.app.business.domain.models.Post

data class RefreshViewState(
    val shouldRefreshAccount: Boolean = false,
    val postToRefresh: Post? = null,
    val postToAdd: Post? = null,
    val postToRemove: String? = null,
)