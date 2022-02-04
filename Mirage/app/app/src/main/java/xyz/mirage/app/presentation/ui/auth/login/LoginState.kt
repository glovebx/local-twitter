package xyz.mirage.app.presentation.ui.auth.login

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.presentation.core.validation.EmailState
import xyz.mirage.app.presentation.core.validation.PasswordState

data class LoginState(
    val isLoading: Boolean = false,
    val email: EmailState = EmailState(),
    val password: PasswordState = PasswordState(),
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
)
