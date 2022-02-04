package xyz.mirage.app.presentation.ui.main.home.create

import android.net.Uri
import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Account
import xyz.mirage.app.presentation.core.validation.PostTextState

data class CreatePostState(
    val account: Account? = null,
    val isLoading: Boolean = false,
    val text: PostTextState = PostTextState(),
    val uri: Uri? = null,
    val onPublishSuccess: Boolean = false,
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
)
