package xyz.mirage.app.presentation.core.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import xyz.mirage.app.BuildConfig
import java.io.File

object FileUtils {

//    Provider path for a specific path as followings:
//        <files-path/> --> Context.getFilesDir()
//        <cache-path/> --> Context.getCacheDir()
//        <external-path/> --> Environment.getExternalStorageDirectory()
//        <external-files-path/> --> Context.getExternalFilesDir(String)
//        <external-cache-path/> --> Context.getExternalCacheDir()
//        <external-media-path/> --> Context.getExternalMediaDirs()

    fun getExternalStorageDir(context: Context, albumName: String = "twitter"): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), albumName)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun getTweetsJsonStringList(context: Context): List<String> {
        val jsonStringList = mutableListOf<String>()
        val twitter = getExternalStorageDir(context = context)
        twitter.walkTopDown().filter { file -> file.extension == "json" }.forEach {
            // convert file content into json object
            jsonStringList.add(it.readText())
        }
        return jsonStringList
    }
//
//    fun getTmpFileUri(context: Context): Uri {
//        val tmpFile = File.createTempFile("tmp_image_file", ".png", context.cacheDir).apply {
//            createNewFile()
//            deleteOnExit()
//        }
//        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
//    }
}
