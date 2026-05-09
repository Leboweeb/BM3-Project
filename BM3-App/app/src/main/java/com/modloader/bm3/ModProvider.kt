package com.modloader.bm3

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import androidx.core.content.edit


class ModProvider : ContentProvider() {



    private enum class StringKeys(s: String) {
        SharedPrefsKey("com.modloader.bm3"),
        CurrentSaveKey("currentSave");

        val string: String = s
    }

    companion object {
        private const val AUTHORITY = "com.modloader.bm3.modprovider"
        const val CODE_SAVE = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "latestSave", CODE_SAVE)
        }
    }

    // unused overrides
    override fun getType(uri: Uri): String = "application/octet-stream"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun onCreate(): Boolean = true


    // actually useful overrides

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        when(uriMatcher.match(uri)) {
            CODE_SAVE -> {
                val currentFile = getCurrentZip()
                return if(currentFile != null && currentFile.exists()) {
                    ParcelFileDescriptor.open(currentFile, ParcelFileDescriptor.MODE_READ_ONLY)
                }
                else {
                    throw FileNotFoundException("The current zip file  has not been generated yet.")
                }
            }
        }
        return null
    }

    private fun getCurrentZip(): File? {
        if (context == null) {
            return null
        }
        val sharedPrefs = context!!.getSharedPreferences(StringKeys.SharedPrefsKey.string, Context.MODE_PRIVATE)
        var currentSaveName = sharedPrefs.getString(StringKeys.CurrentSaveKey.string, "") ?: ""
        if (currentSaveName.isBlank()) {
            val firstFile = context!!.cacheDir?.listFiles()?.elementAtOrNull(0) ?: return null
            // if no file in shared prefs, put first found zip file in shared prefs instead
            sharedPrefs.edit { putString(StringKeys.CurrentSaveKey.string, firstFile.name) }
            currentSaveName = firstFile.name
        }
        return File(context!!.cacheDir, currentSaveName)
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        val file = getCurrentZip() ?: return null

        // Standard columns for file metadata
        val columns = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns).apply {
            if (file.exists()) {
                newRow()
                    .add(OpenableColumns.DISPLAY_NAME, "latest_save.zip")
                    .add(OpenableColumns.SIZE, file.length())
            }
        }
    }

}