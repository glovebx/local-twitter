package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mirage.app.R

@Composable
fun RetweetLabel(
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.padding(start = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_retweet),
            contentDescription = "IsRetweet Icon"
        )
        Text(
            text = "Retweet",
            color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.size(2.dp))
}