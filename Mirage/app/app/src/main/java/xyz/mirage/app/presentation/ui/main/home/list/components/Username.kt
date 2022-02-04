package xyz.mirage.app.presentation.ui.main.home.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.presentation.core.util.DateUtils

@Composable
fun Username(
    post: Post,
    authId: String,
    handleFollow: (String) -> Unit,
    handleDeletePost: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.width(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = post.profile.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "@${post.profile.username}",
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.size(5.dp))

            Text(
                text = "· ${DateUtils.relativeTime(post.createdAt)}",
                color = Color.Gray,
            )
        }

        Box {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = Color.Gray,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        showMenu = true
                    }
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
//                if (authId != post.profile.id) {
//                    DropdownMenuItem(
//                        onClick = {
//                            handleFollow(post.profile.username)
//                            showMenu = false
//                        }
//                    ) {
//                        Text(text = if (post.profile.following) "Unfollow" else "Follow")
//                    }
//                } else {
                    DropdownMenuItem(
                        onClick = {
                            handleDeletePost(post.id)
                            showMenu = false
                        }
                    ) {
                        Text(text = "Delete")
                    }
//                }
            }
        }
    }
}
