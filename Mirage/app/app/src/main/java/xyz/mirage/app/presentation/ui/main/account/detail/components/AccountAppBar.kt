package xyz.mirage.app.presentation.ui.main.account.detail.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.mirage.app.presentation.core.theme.DarkBackgroundColor
import xyz.mirage.app.presentation.core.theme.LightBackgroundColor

@Composable
fun AccountAppBar(
    handleLogout: () -> Unit,
    scrollState: LazyListState,
    isDarkTheme: Boolean,
    onScrollToTop: () -> Unit,
) {
    val showAppbar = scrollState.firstVisibleItemIndex > 0

    val transition = updateTransition(targetState = showAppbar, label = "transition")
    val appBarColor by transition.animateColor(label = "AppbarColor") { isVisible ->
        when {
            isVisible && isDarkTheme -> DarkBackgroundColor
            isVisible -> LightBackgroundColor
            else -> Color.Black.copy(alpha = 0.4f)
        }
    }

    val textColor = when {
        showAppbar && !isDarkTheme -> Color.Black
        else -> Color.White
    }

    TopAppBar(
        elevation = 1.dp,
        title = {
            Text(
                text = "Account",
                color = textColor,
            )
        },
        modifier = Modifier.clickable { onScrollToTop() },
        backgroundColor = appBarColor,
        actions = {
            IconButton(
                onClick = {
                    handleLogout()
                }
            ) {
                Row(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        tint = textColor,
                        contentDescription = "Logout"
                    )
                }
            }
        }
    )
}