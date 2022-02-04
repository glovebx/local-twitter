package xyz.mirage.app.presentation.ui.auth.login

sealed class LoginEvents {

    object Login : LoginEvents()

    object OnRemoveHeadFromQueue : LoginEvents()
}