package xyz.mirage.app.presentation.ui.main.home.list

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Post

data class PostListState(
    val isLoading: Boolean = false,
    val page: Int = 1,
    val posts: List<Post> = listOf(),
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
    val isRefreshing: Boolean = false,
    val deletedCount: Int = 0,
)
