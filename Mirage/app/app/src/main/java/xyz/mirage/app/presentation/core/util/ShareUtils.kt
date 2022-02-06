package xyz.mirage.app.presentation.core.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ShareUtils {

    private const val WX_PACKAGE_NAME = "com.tencent.mm"
    private const val WX_SHARE_TO_CHAT_ACTIVITY = "com.tencent.mm.ui.tools.ShareImgUI"
    private const val WX_SHARE_SCREEN_TO_TIMELINE_ACTIVITY = "com.tencent.mm.ui.tools.ShareScreenToTimeLineUI"

    // 发送到微信
    fun send(context: Context, filetype: String, uriList: ArrayList<Uri>) {

        val intent = Intent().apply {
            component = ComponentName(WX_PACKAGE_NAME, WX_SHARE_TO_CHAT_ACTIVITY)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        if (filetype == "video") {
            // 发到群里
            intent.apply {
                type = "video/*"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uriList[0])
            }
        } else {
            // 分享多个图片给好友
            intent.apply {
                type = "image/*"
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
            }
        }
        context.startActivity(Intent.createChooser(intent, "Mirage"))
    }

    // 分享到朋友圈
    fun share(context: Context, filetype: String, uriList: ArrayList<Uri>) {

    }
}
