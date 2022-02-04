package xyz.mirage.app.presentation.ui.main.account.detail

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Account
import xyz.mirage.app.business.domain.models.Post

data class AccountState(
    val shouldLoad: Boolean = true,
    val isLoading: Boolean = false,
    val postsIsLoading: Boolean = false,
    val account: Account? = null,
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
    val currentTab: Int = 0,
    val page: Int = 1,
    val posts: List<Post> = listOf(),
    val isRefreshing: Boolean = false
)