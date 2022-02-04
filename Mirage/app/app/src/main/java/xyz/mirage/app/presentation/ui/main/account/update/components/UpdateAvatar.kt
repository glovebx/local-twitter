package xyz.mirage.app.presentation.ui.main.account.update.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import xyz.mirage.app.business.domain.core.ErrorHandling.Companion.ERROR_SOMETHING_WRONG_WITH_IMAGE
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.Response
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.business.domain.core.UIComponentType
import xyz.mirage.app.presentation.core.theme.DarkBackgroundColor
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountEvents
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountEvents.OnMessageReceived
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountState

@ExperimentalCoilApi
@Composable
fun UpdateAvatar(
    state: UpdateAccountState,
    modifier: Modifier,
    isDarkTheme: Boolean,
    imageLoader: ImageLoader,
    onTriggerEvent: (UpdateAccountEvents) -> Unit,
) {
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                onTriggerEvent(UpdateAccountEvents.OnUpdateImage(uri))
            }
        } else {
            onTriggerEvent(
                OnMessageReceived(
                    stateMessage = StateMessage(
                        response = Response(
                            message = ERROR_SOMETHING_WRONG_WITH_IMAGE,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        )
                    )
                )
            )
        }
    }

    state.account?.let { user ->

        val url = state.imageURI ?: user.image
        val painter = rememberImagePainter(
            data = url,
            imageLoader = imageLoader,
        )

        Image(
            painter = painter,
            contentDescription = "Account Avatar",
            modifier = modifier
                .size(100.dp)
                .offset(20.dp, 60.dp)
                .clip(shape = CircleShape)
                .border(
                    border = BorderStroke(
                        width = 3.dp,
                        color = if (isDarkTheme) DarkBackgroundColor else Color.White
                    ),
                    shape = CircleShape
                ),
            contentScale = ContentScale.Crop
        )

        when (painter.state) {
            is ImagePainter.State.Loading -> {
                Box(
                    modifier = modifier
                        .background(Color.Transparent)
                        .size(100.dp)
                        .offset(20.dp, 60.dp)
                        .clip(shape = CircleShape)
                        .border(
                            border = BorderStroke(
                                width = 3.dp,
                                color = if (isDarkTheme) DarkBackgroundColor else Color.White
                            ),
                            shape = CircleShape
                        ),
                )
            }
            else -> {
            }
        }
    }

    Box(
        modifier = modifier
            .size(100.dp)
            .offset(20.dp, 60.dp)
            .clip(shape = CircleShape)
            .background(Color.Black.copy(0.5f))
            .border(
                border = BorderStroke(
                    width = 3.dp,
                    color = if (isDarkTheme) DarkBackgroundColor else Color.White
                ),
                shape = CircleShape
            )
            .clickable {
                cropLauncher.launch(
                    options {
                        setGuidelines(CropImageView.Guidelines.ON)
                        setAspectRatio(1, 1)
                        setCropShape(CropImageView.CropShape.OVAL)
                    }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Outlined.AddAPhoto,
            contentDescription = "Pick Avatar",
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.Center),
            tint = Color.White,
        )
    }
}