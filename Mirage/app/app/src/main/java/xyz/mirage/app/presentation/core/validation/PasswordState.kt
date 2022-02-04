package xyz.mirage.app.presentation.core.validation

import xyz.mirage.app.presentation.core.validation.PasswordState.Companion.PASSWORD_MAX_LENGTH
import xyz.mirage.app.presentation.core.validation.PasswordState.Companion.PASSWORD_MIN_LENGTH

class PasswordState : TextFieldState(
    validator = ::isPasswordValid,
    errorFor = ::passwordValidationError
) {
    companion object {
        const val PASSWORD_MIN_LENGTH = 6
        const val PASSWORD_MAX_LENGTH = 150
    }
}

private fun isPasswordValid(password: String): Boolean {
    return password.length in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun passwordValidationError(password: String): String {
    return "The password must be at least $PASSWORD_MIN_LENGTH characters"
}