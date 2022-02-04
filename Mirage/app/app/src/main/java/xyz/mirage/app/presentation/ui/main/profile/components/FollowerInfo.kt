package xyz.mirage.app.presentation.ui.main.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mirage.app.business.domain.models.Profile

@Composable
fun FollowerInfo(
    user: Profile
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "${user.followee} ",
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Following",
            fontSize = 14.sp,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = "${user.followers} ",
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Followers",
            fontSize = 14.sp,
            color = Color.Gray,
        )
    }
}