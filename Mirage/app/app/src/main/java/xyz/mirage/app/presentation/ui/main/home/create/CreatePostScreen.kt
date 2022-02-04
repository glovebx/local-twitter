package xyz.mirage.app.presentation.ui.main.home.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostEvents.OnRemoveHeadFromQueue
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostEvents.PublishPost
import xyz.mirage.app.presentation.ui.main.home.create.components.AvatarWithTextField
import xyz.mirage.app.presentation.ui.main.home.create.components.CreateBar
import xyz.mirage.app.presentation.ui.main.home.create.components.CreatePostAppBar
import xyz.mirage.app.presentation.ui.shared.CustomDivider

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@Composable
fun CreatePostScreen(
    isDarkTheme: Boolean,
    isNetworkAvailable: Boolean,
    navController: NavController,
    state: CreatePostState,
    onTriggerEvent: (CreatePostEvents) -> Unit,
    scaffoldState: ScaffoldState,
    imageLoader: ImageLoader,
) {
    val isDisabled = (!state.text.isValid || state.text.text.isEmpty()) && state.uri == null

    if (state.onPublishSuccess) {
        navController.popBackStack()
    }

    AppTheme(
        darkTheme = isDarkTheme,
        dialogQueue = state.queue,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(OnRemoveHeadFromQueue)
        },
        isNetworkAvailable = isNetworkAvailable,
        displayProgressBar = state.isLoading,
        scaffoldState = scaffoldState
    ) {
        Scaffold(
            topBar = {
                CreatePostAppBar(
                    handleBack = { navController.popBackStack() },
                    handleSubmit = { onTriggerEvent(PublishPost) },
                    isDisabled = isDisabled,
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    AvatarWithTextField(
                        state = state,
                        imageLoader = imageLoader,
                    )
                    state.uri?.let { uri ->
                        val painter = rememberImagePainter(
                            data = uri,
                            imageLoader = imageLoader,
                        )

                        Image(
                            painter = painter,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .heightIn(100.dp)
                                .fillMaxWidth()
                                .clip(shape = RoundedCornerShape(15.dp)),
                        )
                    }
                }

                Column {
                    CustomDivider(isDarkTheme = isDarkTheme)
                    CreateBar(
                        state = state.text,
                        onTriggerEvent = onTriggerEvent
                    )
                }
            }
        }
    }
}