package xyz.mirage.app.presentation.ui.main.home.detail.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@Composable
fun PostDetailAppBar(
    handleBack: () -> Unit,
) {
    TopAppBar(
        title = { Text("Post") },
        backgroundColor = Color.Transparent,
        elevation = 1.dp,
        navigationIcon = {
            IconButton(
                onClick = { handleBack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = PrimaryColor,
                )
            }
        }
    )
}