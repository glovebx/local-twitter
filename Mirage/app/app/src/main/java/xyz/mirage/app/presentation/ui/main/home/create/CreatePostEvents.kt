package xyz.mirage.app.presentation.ui.main.home.create

import android.net.Uri
import xyz.mirage.app.business.domain.core.StateMessage

sealed class CreatePostEvents {

    object PublishPost : CreatePostEvents()

    data class OnUpdateUri(
        val uri: Uri,
    ) : CreatePostEvents()

    data class OnMessageReceived(val stateMessage: StateMessage) : CreatePostEvents()

    object OnRemoveHeadFromQueue : CreatePostEvents()
}