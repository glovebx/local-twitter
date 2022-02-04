package xyz.mirage.app.presentation.core.validation

import xyz.mirage.app.presentation.core.validation.PostTextState.Companion.TEXT_MAX_LENGTH

class PostTextState : TextFieldState(
    validator = ::isTextValid,
    errorFor = ::textValidationError,
) {
    companion object {
        const val TEXT_MAX_LENGTH = 280
    }
}

private fun isTextValid(text: String): Boolean {
    return text.length <= TEXT_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun textValidationError(text: String): String {
    return "You got att most $TEXT_MAX_LENGTH characters"
}