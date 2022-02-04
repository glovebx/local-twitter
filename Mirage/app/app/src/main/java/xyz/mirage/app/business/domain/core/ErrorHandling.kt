package xyz.mirage.app.business.domain.core

class ErrorHandling {

    companion object {
        const val ERROR_SAVE_AUTH_TOKEN =
            "Error saving authentication token.\nTry restarting the app."
        const val ERROR_SOMETHING_WRONG_WITH_IMAGE = "Something went wrong with the image."
        const val ERROR_MUST_SELECT_IMAGE = "Text is required if no file is selected"

        const val GENERIC_AUTH_ERROR = "Error"
        const val UNAUTHORIZED_ERROR = "You are not allowed to do this action"
        const val SERVER_ERROR = "Server Error. Try again later."
        const val INVALID_CREDENTIALS = "Invalid credentials"
        const val ERROR_EMAIL_IN_USE = "The given email address is already in use."
        const val ERROR_USERNAME_IN_USE = "The given username is already in use."
        const val ERROR_PASSWORD_BLANK_FIELD = "Password cannot be blank."
        const val ERROR_DISPLAYNAME_BLANK_FIELD = "DisplayName cannot be blank."
        const val ERROR_EMAIL_BLANK_FIELD = "Email cannot be blank."
        const val ERROR_USERNAME_BLANK_FIELD = "Username cannot be blank."
        const val ERROR_UNABLE_TO_RETRIEVE_ACCOUNT_DETAILS =
            "Unable to retrieve account details. Try logging out."
        const val ERROR_POST_UNABLE_TO_RETRIEVE =
            "Unable to retrieve the post. Maybe it got deleted"
        const val ERROR_NO_PREVIOUS_AUTH_USER =
            "No previously authenticated user. This error can be ignored."
        const val UNKNOWN_ERROR = "Unknown error"
        const val ERROR_PROFILE_UNABLE_TO_RETRIEVE =
            "Unable to retrieve the profile. Try reselecting it from the list."
    }

}

