package xyz.mirage.app.presentation.session

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage

data class SessionState(
    val isLoading: Boolean = false,
    val uid: String? = null,
    val didCheckForPreviousAuthUser: Boolean = false,
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
)