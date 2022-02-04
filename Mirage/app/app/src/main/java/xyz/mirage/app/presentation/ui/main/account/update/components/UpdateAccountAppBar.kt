package xyz.mirage.app.presentation.ui.main.account.update.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@Composable
fun UpdateAccountAppBar(
    handleBack: () -> Unit,
    handleSave: () -> Unit,
) {
    TopAppBar(
        elevation = 0.dp,
        title = { Text(text = "Edit profile") },
        backgroundColor = Color.Transparent,
        navigationIcon = {
            IconButton(
                onClick = { handleBack() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    tint = PrimaryColor,
                    contentDescription = "Back"
                )
            }
        }, actions = {
            Text(
                text = "Save",
                color = PrimaryColor,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .clickable {
                        handleSave()
                    }
            )
        }
    )
}