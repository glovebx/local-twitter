package xyz.mirage.app.presentation.ui.main.account.update

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountEvents.*
import xyz.mirage.app.presentation.ui.main.account.update.components.InputField
import xyz.mirage.app.presentation.ui.main.account.update.components.UpdateAccountAppBar
import xyz.mirage.app.presentation.ui.main.account.update.components.UpdateAvatar
import xyz.mirage.app.presentation.ui.main.account.update.components.UpdateBanner
import xyz.mirage.app.presentation.ui.shared.CircularIndeterminateProgressBar

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAccountScreen(
    id: String?,
    navController: NavController,
    state: UpdateAccountState,
    onTriggerEvent: (UpdateAccountEvents) -> Unit,
    isDarkTheme: Boolean,
    scaffoldState: ScaffoldState,
    isNetworkAvailable: Boolean,
    imageLoader: ImageLoader,
) {

    if (!state.onLoad) {
        onTriggerEvent(ToggleOnLoad)
        id?.let {
            onTriggerEvent(GetAccountFromCache(id = id))
        }
    }

    fun handleBackPress() {
        if (state.isDirty) {
            val callback: AreYouSureCallback = object : AreYouSureCallback {
                override fun proceed() {
                    navController.popBackStack()
                }

                override fun cancel() {}
            }

            val message = StateMessage(
                response = Response(
                    message = "Discard changes?",
                    uiComponentType = UIComponentType.AreYouSureDialog(callback),
                    messageType = MessageType.Info()
                ),
            )
            onTriggerEvent(OnMessageReceived(message))
        } else {
            navController.popBackStack()
        }
    }

    AppTheme(
        isNetworkAvailable = isNetworkAvailable,
        displayProgressBar = state.isLoading,
        darkTheme = isDarkTheme,
        scaffoldState = scaffoldState,
        dialogQueue = state.queue,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(OnRemoveHeadFromQueue)
        }
    ) {
        val controller = LocalSoftwareKeyboardController.current

        Scaffold(
            topBar = {
                UpdateAccountAppBar(
                    handleBack = {
                        handleBackPress()
                    },
                    handleSave = {
                        controller?.hide()
                        onTriggerEvent(UpdatePressed)
                    }
                )
            }
        ) {
            state.account?.let { _ ->
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Box {

                        UpdateBanner(
                            modifier = Modifier.align(Alignment.Center),
                            state = state,
                            onTriggerEvent = onTriggerEvent,
                            imageLoader = imageLoader
                        )

                        UpdateAvatar(
                            modifier = Modifier.align(Alignment.BottomStart),
                            isDarkTheme = isDarkTheme,
                            state = state,
                            onTriggerEvent = onTriggerEvent,
                            imageLoader = imageLoader
                        )
                    }

                    Spacer(modifier = Modifier.size(60.dp))

                    InputField(
                        label = "Email",
                        state = state.email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { controller?.hide() }
                        ),
                        isDarkTheme = isDarkTheme,
                        onTriggerEvent = { onTriggerEvent(SetIsDirty) }
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    InputField(
                        label = "Username",
                        state = state.username,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { controller?.hide() }
                        ),
                        isDarkTheme = isDarkTheme,
                        onTriggerEvent = { onTriggerEvent(SetIsDirty) }
                    )

                    InputField(
                        label = "Display Name",
                        state = state.displayName,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { controller?.hide() }
                        ),
                        isDarkTheme = isDarkTheme,
                        onTriggerEvent = { onTriggerEvent(SetIsDirty) }
                    )

                    InputField(
                        label = "Bio",
                        state = state.bio,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { controller?.hide() }
                        ),
                        isDarkTheme = isDarkTheme,
                        onTriggerEvent = { onTriggerEvent(SetIsDirty) }
                    )
                }
            } ?: CircularIndeterminateProgressBar(isDisplayed = state.isLoading)
        }
    }
}