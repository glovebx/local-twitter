package xyz.mirage.app.presentation.ui.shared

import android.util.Log
import android.widget.Toast
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.core.UIComponentType.AreYouSureDialog
import java.util.*

@Composable
fun ProcessDialogQueue(
    dialogQueue: KQueue<StateMessage>?,
    onRemoveHeadMessageFromQueue: () -> Unit,
    scaffoldState: ScaffoldState,
    isDarkTheme: Boolean
) {
    dialogQueue?.peek()?.let { stateMessage ->
        val response = stateMessage.response

        when (response.uiComponentType) {
            is UIComponentType.Dialog -> {
                DisplayDialog(
                    message = response.message.toString(),
                    type = response.messageType,
                    onRemoveHeadMessageFromQueue = onRemoveHeadMessageFromQueue,
                    isDarkTheme = isDarkTheme,
                )
            }

            is AreYouSureDialog -> {
                DisplayAreYouSureDialog(
                    message = response.message.toString(),
                    callback = response.uiComponentType.callback,
                    onRemoveHeadMessageFromQueue = onRemoveHeadMessageFromQueue,
                    isDarkTheme = isDarkTheme,
                )
            }

            is UIComponentType.Snackbar -> {
                val scope = rememberCoroutineScope()

                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = response.message.toString(),
                        actionLabel = "Hide"
                    )
                    onRemoveHeadMessageFromQueue()
                }
            }

            is UIComponentType.Toast -> {
                val context = LocalContext.current
                Toast.makeText(context, response.message.toString(), Toast.LENGTH_LONG)
                onRemoveHeadMessageFromQueue()
            }

            is UIComponentType.None -> {
                Log.i(TAG, "onResponseReceived: ${response.message}")
                onRemoveHeadMessageFromQueue()
            }
        }
    }
}

@Composable
private fun DisplayDialog(
    message: String,
    type: MessageType,
    onRemoveHeadMessageFromQueue: () -> Unit,
    isDarkTheme: Boolean
) {

    val title = when (type) {
        is MessageType.Error -> "Error"
        is MessageType.Info -> "Info"
        is MessageType.None -> "None"
        is MessageType.Success -> "Success"
    }

    val dialogInfo = GenericMessageInfo.Builder()
        .id(UUID.randomUUID().toString())
        .title(title)
        .positive(PositiveAction("Ok") { onRemoveHeadMessageFromQueue() })
        .description(message)

    GenericDialog(
        onDismiss = dialogInfo.onDismiss,
        title = dialogInfo.title ?: "",
        description = dialogInfo.description,
        positiveAction = dialogInfo.positiveAction,
        negativeAction = dialogInfo.negativeAction,
        onRemoveHeadFromQueue = onRemoveHeadMessageFromQueue,
        isDarkTheme = isDarkTheme
    )
}

@Composable
private fun DisplayAreYouSureDialog(
    message: String,
    callback: AreYouSureCallback,
    onRemoveHeadMessageFromQueue: () -> Unit,
    isDarkTheme: Boolean
) {

    val dialogInfo = GenericMessageInfo.Builder()
        .id(UUID.randomUUID().toString())
        .title("Confirmation")
        .positive(PositiveAction("Ok") {
            callback.proceed()
            onRemoveHeadMessageFromQueue()
        })
        .negative(NegativeAction("Cancel") {
            callback.cancel()
            onRemoveHeadMessageFromQueue()
        })
        .description(message)

    GenericDialog(
        onDismiss = dialogInfo.onDismiss,
        title = dialogInfo.title.toString(),
        description = dialogInfo.description,
        positiveAction = dialogInfo.positiveAction,
        negativeAction = dialogInfo.negativeAction,
        onRemoveHeadFromQueue = onRemoveHeadMessageFromQueue,
        isDarkTheme = isDarkTheme
    )
}