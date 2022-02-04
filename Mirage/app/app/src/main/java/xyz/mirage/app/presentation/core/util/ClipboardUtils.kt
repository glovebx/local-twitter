package xyz.mirage.app.presentation.core.util

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtils {

    fun setClipboard(context: Context, content: String): Unit {
        if (content.isNullOrEmpty()) return
        (context.getSystemService(Service.CLIPBOARD_SERVICE) as ClipboardManager).run {
            this.setPrimaryClip(ClipData.newPlainText("mirage", content))
        }
    }

}
