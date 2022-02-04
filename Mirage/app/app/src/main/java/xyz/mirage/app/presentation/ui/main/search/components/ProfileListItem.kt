package xyz.mirage.app.presentation.ui.main.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import xyz.mirage.app.business.domain.models.Profile
import xyz.mirage.app.presentation.ui.main.home.list.components.Avatar

@ExperimentalCoilApi
@Composable
fun ProfileListItem(
    profile: Profile,
    onNavigateToProfileScreen: () -> Unit,
    imageLoader: ImageLoader
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp)
            .clickable {
                onNavigateToProfileScreen()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            profile = profile,
            onNavigateToProfileScreen = onNavigateToProfileScreen,
            imageLoader = imageLoader
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(
                text = profile.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = "@${profile.username}",
                color = Color.Gray,
                fontSize = 14.sp,
            )
        }
    }
}