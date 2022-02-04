package xyz.mirage.app.presentation.ui.main.home.detail

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Post

data class PostDetailState(
    val onLoad: Boolean = false,
    val isLoading: Boolean = false,
    val post: Post? = null,
    val queue: KQueue<StateMessage> = KQueue(mutableListOf())
)