package xyz.mirage.app.presentation.ui.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import xyz.mirage.app.presentation.core.theme.AppTheme
import xyz.mirage.app.presentation.ui.auth.components.*
import xyz.mirage.app.presentation.ui.auth.register.RegisterEvents.OnRemoveHeadFromQueue
import xyz.mirage.app.presentation.ui.auth.register.RegisterEvents.RegisterClicked

@ExperimentalComposeUiApi
@Composable
fun RegisterScreen(
    isNetworkAvailable: Boolean,
    scaffoldState: ScaffoldState,
    navController: NavController,
    state: RegisterState,
    onTriggerEvent: (RegisterEvents) -> Unit,
    isDarkTheme: Boolean,
) {

    val (displayRequest, emailRequest, passwordRequest) = FocusRequester.createRefs()
    val controller = LocalSoftwareKeyboardController.current

    AppTheme(
        darkTheme = isDarkTheme,
        isNetworkAvailable = isNetworkAvailable,
        displayProgressBar = state.isLoading,
        scaffoldState = scaffoldState,
        dialogQueue = state.queue,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(OnRemoveHeadFromQueue)
        }
    ) {
        Scaffold(
            topBar = {
                AuthTopBar(
                    handleBack = { navController.popBackStack() }
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
            ) {
                AuthHeadline(text = "Create your account")

                Spacer(modifier = Modifier.size(100.dp))

                AuthTextField(
                    placeholder = "Username",
                    state = state.username,
                    keyboardActions = KeyboardActions(
                        onNext = { displayRequest.requestFocus() }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isDarkTheme = isDarkTheme,
                )

                Spacer(modifier = Modifier.size(10.dp))

                AuthTextField(
                    placeholder = "Display Name",
                    state = state.displayName,
                    keyboardActions = KeyboardActions(
                        onNext = { emailRequest.requestFocus() }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    requester = displayRequest,
                    isDarkTheme = isDarkTheme,
                )

                Spacer(modifier = Modifier.size(10.dp))

                AuthTextField(
                    placeholder = "Email",
                    state = state.email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordRequest.requestFocus() }
                    ),
                    requester = emailRequest,
                    isDarkTheme = isDarkTheme,
                )

                Spacer(modifier = Modifier.size(10.dp))

                PasswordField(
                    state = state.password,
                    isDarkTheme = isDarkTheme,
                    passwordRequest = passwordRequest,
                    keyboardActions = KeyboardActions(
                        onDone = { controller?.hide() }
                    )
                )

                Spacer(modifier = Modifier.size(100.dp))

                AuthButton(
                    text = "Sign up",
                    handleClick = {
                        controller?.hide()
                        onTriggerEvent(RegisterClicked)
                    },
                    isEnabled = state.email.isValid && state.username.isValid && state.password.isValid && state.displayName.isValid
                )

            }
        }
    }

}
