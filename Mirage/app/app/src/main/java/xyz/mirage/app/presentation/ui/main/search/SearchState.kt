package xyz.mirage.app.presentation.ui.main.search

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.domain.models.Profile

data class SearchState(
    val isLoading: Boolean = false,
    val profiles: List<Profile> = listOf(),
    val posts: List<Post> = listOf(),
    val query: String = "",
    val isQueryExhausted: Boolean = false,
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
    val listPosition: Int = 0,
    val page: Int = 0,
)