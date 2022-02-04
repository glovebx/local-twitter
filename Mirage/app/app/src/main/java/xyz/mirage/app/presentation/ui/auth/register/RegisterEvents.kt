package xyz.mirage.app.presentation.ui.auth.register

sealed class RegisterEvents {

    object RegisterClicked : RegisterEvents()

    object OnRemoveHeadFromQueue : RegisterEvents()
}