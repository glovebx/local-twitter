package xyz.mirage.app.presentation.core.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import okhttp3.internal.closeQuietly
import java.io.*

class MediaColumns(private val isVideo: Boolean) {
    val DESCRIPTION = if (isVideo) MediaStore.Video.Media.DESCRIPTION else MediaStore.Images.Media.DESCRIPTION

    val EXTERNAL_CONTENT_URI = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val RELATIVE_DIRECTORY = if (isVideo) Environment.DIRECTORY_MOVIES + "/Mirage" else Environment.DIRECTORY_PICTURES + "/Mirage"
}

object MediaStoreUtils {
    fun create(
        context: Context,
        file: File,
        mimeType: String,
        extra: ContentValues? = null
    ): Uri? {
        val mediaColumns = MediaColumns(mimeType.contains("video"))

        // 如果文件已经存在，只需要更新时间
        val uri = refreshMediaIfExists(context, file, mediaColumns)
        if (uri != null) return uri

        val now = System.currentTimeMillis()
        return ContentValues().run {
            put(MediaStore.MediaColumns.TITLE, file.name)
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(mediaColumns.DESCRIPTION, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            // DATE_TAKEN is in milliseconds since 1970
            // DATE_ADDED、DATE_MODIFIED is in seconds since 1970, so just multiply it by 1000 and it'll be fine
            // NOTE: DATE_MODIFIED is for internal use by the media scanner. Do not modify this field.
            put(MediaStore.MediaColumns.DATE_ADDED, now / 1000)
//            put(MediaStore.MediaColumns.DATE_MODIFIED, now / 1000)
            put(MediaStore.MediaColumns.DATE_TAKEN, now)

            put(MediaStore.MediaColumns.SIZE, file.length())
            put(MediaStore.MediaColumns.RELATIVE_PATH, mediaColumns.RELATIVE_DIRECTORY)

            context.contentResolver.insert(mediaColumns.EXTERNAL_CONTENT_URI, this).also {
                saveMedia(context, it, file)
            }
        }
    }

    private fun saveMedia(context: Context, uri: Uri?, file: File): Unit {
        var inputStream: BufferedInputStream? = null
        var os: OutputStream? = null
        try {
            inputStream = BufferedInputStream(FileInputStream(file))
            if (uri != null) {
                os = context.contentResolver.openOutputStream(uri)
            }
            if (os != null) {
                val buffer = ByteArray(1024 * 4)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    os.write(buffer, 0, len)
                }
                os.flush()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            os?.closeQuietly()
            inputStream?.closeQuietly()
        }
    }

    private fun refreshMediaIfExists(context: Context, file: File, mediaColumns: MediaColumns): Uri? {
        context.contentResolver.query(
            mediaColumns.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            MediaStore.MediaColumns.DISPLAY_NAME + "=? ",
            arrayOf(file.name),
            null
        ).run {
            return if (this != null && this.moveToFirst()) {
                val id: Long = this.getLong(this.getColumnIndex(MediaStore.MediaColumns._ID))
                //            Uri baseUri = Uri.parse("content://media/external/images/media");
                refreshMedia(context, id, mediaColumns)
                ContentUris.withAppendedId(mediaColumns.EXTERNAL_CONTENT_URI, id)
            } else null
        }
    }

    private fun refreshMedia(context: Context, id: Long, mediaColumns: MediaColumns): Int {
        val now = System.currentTimeMillis()
        return ContentValues().run {
            // TODO: 更改媒体的时间，打开相册时图片会显示在最前面
            put(MediaStore.MediaColumns.DATE_TAKEN, now)
            put(MediaStore.MediaColumns.DATE_ADDED, now / 1000)
//            put(MediaStore.MediaColumns.DATE_MODIFIED, now / 1000)

            context.contentResolver.update(
                mediaColumns.EXTERNAL_CONTENT_URI,
                this,
                MediaStore.MediaColumns._ID + "=? ",
                arrayOf("" + id)
            )
        }
    }
//
//    fun update(context: Context, uri: Uri, values: ContentValues) {
//        context.contentResolver.update(uri, values, null, null)
//    }
//
//    fun delete(context: Context, uri: Uri) {
//        context.contentResolver.delete(uri, null, null)
//    }
//
//    fun delete(context: Context, uris: List<Uri>) {
//        val operations = ArrayList<ContentProviderOperation>(uris.size)
//        uris.forEach { operations.add(ContentProviderOperation.newDelete(it).build()) }
//        context.contentResolver.applyBatch(MediaStore.AUTHORITY, operations)
//    }
//
//    fun rename(context: Context, uri: Uri, newName: String) {
//        val values = ContentValues()
//        //values.put(MediaStore.Video.Media.TITLE, newName)
//        values.put(MediaStore.Video.Media.DISPLAY_NAME, newName)
//        // DATE_MODIFIED is in secondsContentProviderOperation
//        //values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
//        context.contentResolver.update(uri, values, null, null)
//    }
}