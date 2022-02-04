package xyz.mirage.app.presentation.ui.main.home.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.mirage.app.business.domain.models.Post

@Composable
fun PostStats(
    post: Post
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row {
            Text(
                text = "${post.retweets}",
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Retweets",
                color = Color.Gray,
            )
        }

        Row {
            Text(
                text = "${post.likes}",
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Likes",
                color = Color.Gray,
            )
        }
    }
}