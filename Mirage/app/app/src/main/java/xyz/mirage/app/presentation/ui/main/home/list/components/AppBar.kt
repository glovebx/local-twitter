package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import xyz.mirage.app.R

@Composable
fun TopBar(
    onScrollToTop: () -> Unit,
    isDarkTheme: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clickable { onScrollToTop() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_twitter),
                contentDescription = "Scroll to top",
                modifier = Modifier.size(22.dp)
            )
        }
        Divider(
            color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color(0xFF333333),
            thickness = 0.2.dp
        )
    }
}