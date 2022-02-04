package xyz.mirage.app.presentation.ui.auth.register

import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.presentation.core.validation.DisplayNameState
import xyz.mirage.app.presentation.core.validation.EmailState
import xyz.mirage.app.presentation.core.validation.PasswordState
import xyz.mirage.app.presentation.core.validation.UsernameState

data class RegisterState(
    val isLoading: Boolean = false,
    val email: EmailState = EmailState(),
    val username: UsernameState = UsernameState(),
    val displayName: DisplayNameState = DisplayNameState(),
    val password: PasswordState = PasswordState(),
    val queue: KQueue<StateMessage> = KQueue(mutableListOf()),
)
