package xyz.mirage.app.presentation.ui.main.profile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mirage.app.presentation.core.util.DateUtils

private val padding = 16.dp

@Composable
fun DisplayName(
    displayName: String
) {
    Text(
        text = displayName,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(horizontal = padding)
    )
}

@Composable
fun Username(
    username: String
) {
    Text(
        text = "@${username}",
        color = Color.Gray,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = padding)
    )
}

@Composable
fun Biography(
    bio: String
) {
    Text(
        text = bio,
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = padding)
    )
}

@Composable
fun JoinDate(
    date: String
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarViewMonth,
            contentDescription = "Joined Date",
            tint = Color.Gray,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Joined ${DateUtils.getJoinTime(date)}",
            fontSize = 14.sp,
            color = Color.Gray,
        )
    }
}