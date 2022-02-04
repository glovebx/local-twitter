package xyz.mirage.app.presentation.ui.auth.login

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
import xyz.mirage.app.presentation.ui.auth.login.LoginEvents.Login
import xyz.mirage.app.presentation.ui.auth.login.LoginEvents.OnRemoveHeadFromQueue

@ExperimentalComposeUiApi
@Composable
fun LoginScreen(
    isNetworkAvailable: Boolean,
    scaffoldState: ScaffoldState,
    navController: NavController,
    onTriggerEvent: (LoginEvents) -> Unit,
    state: LoginState,
    isDarkTheme: Boolean,
) {
    val (passwordRequest) = FocusRequester.createRefs()
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
            }
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
            ) {
                AuthHeadline(text = "Log in to Mirage")

                Spacer(modifier = Modifier.size(100.dp))

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
                    text = "Log in",
                    handleClick = {
                        controller?.hide()
                        onTriggerEvent(Login)
                    },
                    isEnabled = state.email.isValid && state.password.isValid
                )
            }
        }
    }
}
