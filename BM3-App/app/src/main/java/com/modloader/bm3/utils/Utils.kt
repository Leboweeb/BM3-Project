package com.modloader.bm3.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


enum class ModProviderStringKeys(s: String) {
    SharedPrefsKey("com.modloader.bm3"), CurrentSaveKey("currentSave");

    val string: String = s
}


private class SharedPreferencesNullException(override val message: String?) : Exception(message)

class ModNotFoundException(override val message: String?) : Exception(message)

fun getPrefs(context: Context?): SharedPreferences {
    val prefs =
        context?.getSharedPreferences(
            ModProviderStringKeys.SharedPrefsKey.string,
            Context.MODE_PRIVATE
        )
            ?: throw SharedPreferencesNullException("Shared preferences is null, context is $context")
    return prefs

}

internal class GithubApiRateLimitInterceptor(
    private val context: Context
) : Interceptor {
    // interceptors reuse same instance of class, no need for companion object
    private var rateLimitUsed = 0
    private val rateLimitMax = 60

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        rateLimitUsed = (response.headers[REMAINING_RATELIMIT_GITHUB] ?: "0").toInt()
        if (rateLimitUsed >= rateLimitMax) {
            val errMsg = "Too many requests, try again later."
            Log.e(BM3_DATA_GITHUBMODMANAGER_TAG, errMsg)
            showToast(context, errMsg)
        }
        return response
    }

}

// Source - https://stackoverflow.com/a/41005913
// Posted by Mike Laren, modified by community. See post 'Timeline' for change history
// Retrieved 2026-09-18, License - CC BY-SA 4.0
fun showToast(context: Context, text: String?) {
    val handler = Handler(Looper.getMainLooper())
    handler.post { Toast.makeText(context, text, Toast.LENGTH_LONG).show() }
}


fun getDefaultModDownloadsFolder(context: Context): File {
    return File(context.cacheDir, MODS_FOLDER_NAME)
}

fun zipFilesAndFoldersWithoutRoot(directoryToZip: File, destinationFile: File) {
    ZipOutputStream(destinationFile.outputStream().buffered()).use { zipOutputStream ->
        directoryToZip.walkTopDown().forEach { file ->
            // prevent file from zipping itself if in same directory as files.
            // also, canonical path resolves symlink edge case
            if (file.isDirectory || (file.canonicalPath == destinationFile.canonicalPath)) {
                return@forEach
            }
            val zipEntryPath = file.relativeTo(directoryToZip).path.replace('\\', '/')
            zipOutputStream.putNextEntry(ZipEntry(zipEntryPath))
            file.inputStream().buffered().use { inputStream ->
                inputStream.copyTo(zipOutputStream)
            }
        }
    }


}

