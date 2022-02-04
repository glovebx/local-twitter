package xyz.mirage.app.presentation.core.validation

import xyz.mirage.app.presentation.core.validation.BiographyState.Companion.BIO_MAX_LENGTH

class BiographyState : TextFieldState(
    validator = ::isBioValid,
    errorFor = ::bioValidationError,
    maxLength = ::bioMaxLength
) {
    companion object {
        const val BIO_MAX_LENGTH = 160
    }
}

private fun isBioValid(bio: String): Boolean {
    return bio.length <= BIO_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun bioMaxLength(bio: String): Int {
    return BIO_MAX_LENGTH
}

@Suppress("UNUSED_PARAMETER")
private fun bioValidationError(bio: String): String {
    return "Bio must be less than $BIO_MAX_LENGTH characters"
}