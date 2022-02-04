package xyz.mirage.app.presentation.ui.main.account.update

import android.net.Uri
import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.models.Account
import xyz.mirage.app.presentation.core.validation.BiographyState
import xyz.mirage.app.presentation.core.validation.DisplayNameState
import xyz.mirage.app.presentation.core.validation.EmailState
import xyz.mirage.app.presentation.core.validation.UsernameState

data class UpdateAccountState(
    // System variables
    val onLoad: Boolean = false,
    val isLoading: Boolean = false,
    val account: Account? = null,
    val isDirty: Boolean = false,

    // Fields to be updated
    val username: UsernameState = UsernameState(),
    val displayName: DisplayNameState = DisplayNameState(),
    val email: EmailState = EmailState(),
    val bio: BiographyState = BiographyState(),
    var imageURI: Uri? = null,
    var bannerURI: Uri? = null,

    // Message Queue
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
)