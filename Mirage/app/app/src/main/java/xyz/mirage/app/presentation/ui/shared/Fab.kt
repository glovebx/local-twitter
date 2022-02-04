package xyz.mirage.app.presentation.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import xyz.mirage.app.R
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@Composable
fun CreatePostFab(
    handleClick: () -> Unit,
) {
    FloatingActionButton(
        backgroundColor = PrimaryColor,
        onClick = { handleClick() },
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_compose),
            contentDescription = "Create post"
        )
    }
}