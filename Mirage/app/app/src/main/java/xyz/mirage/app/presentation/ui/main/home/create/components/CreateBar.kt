package xyz.mirage.app.presentation.ui.main.home.create.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.options
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.presentation.core.theme.PrimaryColor
import xyz.mirage.app.presentation.core.validation.PostTextState
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostEvents
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostEvents.OnMessageReceived
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostEvents.OnUpdateUri

@Composable
fun CreateBar(
    state: PostTextState,
    onTriggerEvent: (CreatePostEvents) -> Unit,
) {
    val pickLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.originalUri?.let { uri ->
                onTriggerEvent(OnUpdateUri(uri))
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                pickLauncher.launch(
                    options {
                        setInitialCropWindowPaddingRatio(0f)
                        setCropMenuCropButtonTitle("Select")
                    }
                )
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                tint = PrimaryColor,
                contentDescription = "Select File"
            )
        }

        TextLimitProgress(text = state.text)
    }
}