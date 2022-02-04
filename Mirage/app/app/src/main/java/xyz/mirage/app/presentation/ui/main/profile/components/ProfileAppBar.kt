package xyz.mirage.app.presentation.ui.main.profile.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@Composable
fun ProfileAppBar(
    username: String?,
    scrollState: LazyListState,
    handleBack: () -> Unit,
) {
    val showAppbar = scrollState.firstVisibleItemIndex > 0

    val transition = updateTransition(targetState = showAppbar, label = "transition")
    val appBarColor by transition.animateColor(label = "AppbarColor") { isVisible ->
        if (isVisible) PrimaryColor else Color.Transparent
    }

    TopAppBar(
        elevation = 0.dp,
        title = {
            Text(
                text = if (showAppbar) username.toString() else "",
                color = Color.White,
            )
        },
        backgroundColor = appBarColor,
        navigationIcon = {
            IconButton(
                onClick = { handleBack() }
            ) {
                Row(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        tint = Color.White,
                        contentDescription = "Back"
                    )
                }
            }
        }, actions = {
            IconButton(
                onClick = {}
            ) {
                Row(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        tint = Color.White,
                        contentDescription = "Options"
                    )
                }
            }
        }
    )
}