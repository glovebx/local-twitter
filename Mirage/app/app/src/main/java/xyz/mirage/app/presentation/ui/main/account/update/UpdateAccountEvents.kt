package xyz.mirage.app.presentation.ui.main.account.update

import android.net.Uri
import xyz.mirage.app.business.domain.core.StateMessage

sealed class UpdateAccountEvents {

    data class GetAccountFromCache(
        val id: String
    ) : UpdateAccountEvents()

    object UpdatePressed : UpdateAccountEvents()

    data class OnUpdateImage(
        val uri: Uri,
    ) : UpdateAccountEvents()

    data class OnUpdateBanner(
        val uri: Uri,
    ) : UpdateAccountEvents()

    object SetIsDirty : UpdateAccountEvents()

    object ToggleOnLoad : UpdateAccountEvents()

    object OnRemoveHeadFromQueue : UpdateAccountEvents()

    data class OnMessageReceived(val stateMessage: StateMessage) : UpdateAccountEvents()
}