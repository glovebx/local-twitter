package xyz.mirage.app.business.domain.models

import android.content.Context
import xyz.mirage.app.presentation.core.util.FileUtils
import java.io.File

data class Attachment(
    var url: String,
    val filetype: String,
    val filename: String,
) {
    val mimeType: String
        get() = if (filetype == "video") "video/3gp" else "image/jpg"
}

fun Attachment.toFile(context: Context, username: String): File {
    return File(File(FileUtils.getExternalStorageDir(context), username), filename)
}