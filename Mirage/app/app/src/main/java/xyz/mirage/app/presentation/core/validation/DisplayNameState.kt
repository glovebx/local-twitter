package xyz.mirage.app.presentation.core.validation

import xyz.mirage.app.presentation.core.validation.DisplayNameState.Companion.DISPLAY_NAME_MAX_LENGTH
import xyz.mirage.app.presentation.core.validation.DisplayNameState.Companion.DISPLAY_NAME_MIN_LENGTH

class DisplayNameState : TextFieldState(
    validator = ::isDisplayNameValid,
    errorFor = ::displayNameValidationError,
    maxLength = ::displayNameMaxLength
) {
    companion object {
        const val DISPLAY_NAME_MIN_LENGTH = 4
        const val DISPLAY_NAME_MAX_LENGTH = 50
    }
}

private fun isDisplayNameValid(displayName: String): Boolean {
    return displayName.length in DISPLAY_NAME_MIN_LENGTH..DISPLAY_NAME_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun displayNameMaxLength(displayName: String): Int {
    return DISPLAY_NAME_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun displayNameValidationError(displayName: String): String {
    return "Display Name must be between $DISPLAY_NAME_MIN_LENGTH and $DISPLAY_NAME_MAX_LENGTH characters"
}