package xyz.mirage.app.presentation.ui.main.home.list.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.domain.models.toFile
import xyz.mirage.app.presentation.core.util.ClipboardUtils
import xyz.mirage.app.presentation.core.util.MediaStoreUtils
import xyz.mirage.app.presentation.core.util.ShareUtils

@Composable
fun PostActions(
    post: Post,
    onToggleLike: (String) -> Unit,
    onToggleRetweet: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(30.dp)
    ) {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnimatedRetweetButton(
                modifier = Modifier,
                isRetweeted = post.retweeted,
                onToggle = {
                    coroutineScope.launch {
                        val shareUriList = withContext(Dispatchers.IO) {
                            post.files?.mapNotNull {
                                MediaStoreUtils.create(
                                    context,
                                    it.toFile(context, post.profile.username),
                                    it.mimeType,
                                )
                            }
                        } as ArrayList
                        if (shareUriList.isEmpty()) {
                            Toast.makeText(context, "图片或视频保存失败", Toast.LENGTH_LONG).show()
                        } else {
                            // 1、拷贝文案
                            post.text?.let {
                                ClipboardUtils.setClipboard(context, it)
                            }
                            ShareUtils.share(context, post.file?.filetype!!, shareUriList)
                        }
                    }
                    onToggleRetweet(post.id)
                }
            )

            Text(text = post.retweets.toString())
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnimatedHeartButton(
                modifier = Modifier,
                isLiked = post.liked,
                onToggle = {
                    coroutineScope.launch {
                        val shareUriList = withContext(Dispatchers.IO) {
                            post.files?.mapNotNull {
                                MediaStoreUtils.create(
                                    context,
                                    it.toFile(context, post.profile.username),
                                    it.mimeType,
                                )
                            }
                        } as ArrayList
                        if (shareUriList.isEmpty()) {
                            Toast.makeText(context, "图片或视频保存失败", Toast.LENGTH_LONG).show()
                        } else {
                            // 1、拷贝文案
                            post.text?.let {
                                ClipboardUtils.setClipboard(context, it)
                            }
                            // 2、发送图片
                            ShareUtils.send(context, post.file?.filetype!!, shareUriList)
                        }
                    }
                    // 更新标记
                    onToggleLike(post.id)
                },
            )

            Text(text = post.likes.toString())
        }
    }
}
