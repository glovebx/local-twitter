package xyz.mirage.app.presentation.ui.main.account.update.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.presentation.ui.main.account.detail.components.UserBanner
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountEvents
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountEvents.OnMessageReceived
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountEvents.OnUpdateBanner
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountState

@ExperimentalCoilApi
@Composable
fun UpdateBanner(
    state: UpdateAccountState,
    modifier: Modifier,
    onTriggerEvent: (UpdateAccountEvents) -> Unit,
    imageLoader: ImageLoader,
) {
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                onTriggerEvent(OnUpdateBanner(uri))
            }
        } else {
            onTriggerEvent(
                OnMessageReceived(
                    stateMessage = StateMessage(
                        response = Response(
                            message = ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        )
                    )
                )
            )
        }
    }

    state.account?.let { user ->
        user.banner?.let { banner ->

            // Prefer newly selected image over user's banner
            val url = state.bannerURI ?: banner

            UserBanner(
                url = url.toString(),
                username = user.username,
                imageLoader = imageLoader
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.Black.copy(0.5f))
            .clickable {
                cropLauncher.launch(
                    options {
                        setGuidelines(CropImageView.Guidelines.ON)
                        setCropShape(CropImageView.CropShape.RECTANGLE)
                        setAspectRatio(2, 1)
                    }
                )
            }
    )

    Icon(
        imageVector = Icons.Outlined.AddAPhoto,
        contentDescription = "Pick Banner",
        modifier = modifier.size(40.dp),
        tint = Color.White
    )

}