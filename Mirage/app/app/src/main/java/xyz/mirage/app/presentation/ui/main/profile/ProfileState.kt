package xyz.mirage.app.presentation.ui.main.profile

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.domain.models.Profile

data class ProfileState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val page: Int = 1,
    val posts: List<Post> = listOf(),
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
    val currentTab: Int = 0,
    val isRefreshing: Boolean = false,
    val postListScrollPosition: Int = 0,
    val postsIsLoading: Boolean = false,
)
