package xyz.mirage.app.presentation.core.validation

import xyz.mirage.app.presentation.core.validation.UsernameState.Companion.USERNAME_MAX_LENGTH
import xyz.mirage.app.presentation.core.validation.UsernameState.Companion.USERNAME_MIN_LENGTH

class UsernameState : TextFieldState(
    validator = ::isUsernameValid,
    errorFor = ::usernameValidationError,
    maxLength = ::usernameMaxLength,
) {
    companion object {
        const val USERNAME_MIN_LENGTH = 4
        const val USERNAME_MAX_LENGTH = 15
    }
}

private fun isUsernameValid(username: String): Boolean {
    return username.all { it.isLetterOrDigit() }
            && username.length in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun usernameMaxLength(username: String): Int {
    return USERNAME_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun usernameValidationError(username: String): String {
    return "Username must be between $USERNAME_MIN_LENGTH and $USERNAME_MAX_LENGTH characters"
}