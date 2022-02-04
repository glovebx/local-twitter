package xyz.mirage.app.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import xyz.mirage.app.business.domain.core.KQueue
import xyz.mirage.app.business.domain.core.StateMessage
import xyz.mirage.app.presentation.ui.shared.CircularIndeterminateProgressBar
import xyz.mirage.app.presentation.ui.shared.ConnectivityMonitor
import xyz.mirage.app.presentation.ui.shared.DefaultSnackbar
import xyz.mirage.app.presentation.ui.shared.ProcessDialogQueue

private val DarkColorPalette = darkColors(
    primary = PrimaryColor,
    background = DarkBackgroundColor,
    surface = DarkBackgroundColor,
    primaryVariant = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    error = RedErrorLight,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = PrimaryColor,
    background = LightBackgroundColor,
    surface = LightBackgroundColor,
    primaryVariant = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    error = RedErrorDark,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun LiteAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = { content() }
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dialogQueue: KQueue<StateMessage> = KQueue(mutableListOf()),
    onRemoveHeadMessageFromQueue: () -> Unit,
    isNetworkAvailable: Boolean,
    displayProgressBar: Boolean,
    scaffoldState: ScaffoldState,
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ProcessDialogQueue(
                    dialogQueue = dialogQueue,
                    onRemoveHeadMessageFromQueue = onRemoveHeadMessageFromQueue,
                    scaffoldState = scaffoldState,
                    isDarkTheme = darkTheme,
                )

                Column {
                    ConnectivityMonitor(
                        isNetworkAvailable = isNetworkAvailable,
                        isDarkTheme = darkTheme
                    )
                    content()
                }

                CircularIndeterminateProgressBar(isDisplayed = displayProgressBar)

                DefaultSnackbar(
                    snackbarHostState = scaffoldState.snackbarHostState,
                    onDismiss = {
                        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    )
}