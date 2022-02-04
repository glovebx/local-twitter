package xyz.mirage.app.presentation.session

import xyz.mirage.app.business.domain.models.Account

sealed class SessionEvents {

    object Logout : SessionEvents()

    data class Login(
        val account: Account
    ) : SessionEvents()

    data class CheckPreviousAuthUser(
        val username: String
    ) : SessionEvents()

    object OnRemoveHeadFromQueue : SessionEvents()

}