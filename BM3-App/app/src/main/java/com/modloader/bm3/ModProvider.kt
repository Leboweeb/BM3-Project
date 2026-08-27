package com.modloader.bm3

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import androidx.core.content.edit
import org.json.JSONObject


class ModProvider : ContentProvider() {


    private enum class StringKeys(s: String) {
        SharedPrefsKey("com.modloader.bm3"), CurrentSaveKey("currentSave");

        val string: String = s
    }

    private class SharedPreferencesNullException(override val message: String?) : Exception(message)

    companion object {
        // TODO : make contract class for URIs 
        private const val AUTHORITY = "com.modloader.bm3.modprovider"
        private const val LATEST_SAVE_URI = "latestSave"
        private const val LATEST_CONFIG_URI = "bm3Prefs"
        private const val ISMODDED_KEY = "isModded"
        const val CODE_SAVE = 1
        const val CODE_PREFS = 2
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, LATEST_SAVE_URI, CODE_SAVE)
            addURI(AUTHORITY, LATEST_CONFIG_URI, CODE_PREFS)
        }
    }


    // unused overrides
    override fun getType(uri: Uri): String = "application/octet-stream"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int = 0

    override fun onCreate(): Boolean = true


    // actually useful overrides

    private fun getPrefs(): SharedPreferences {
        val prefs =
            context?.getSharedPreferences(StringKeys.SharedPrefsKey.string, Context.MODE_PRIVATE)
                ?: throw SharedPreferencesNullException("Shared preferences is null, context is $context")
        return prefs

    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        when (uriMatcher.match(uri)) {
            CODE_SAVE -> {
                val currentFile = getCurrentZip()
                try {
                    return if (currentFile != null && currentFile.exists()) {
                        ParcelFileDescriptor.open(currentFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    } else {
                        throw FileNotFoundException("The current zip file  has not been generated yet.")
                    }
                } finally {
                    // finally, delete zip file to not send file on next app open if found
                    if (!BuildConfig.DEBUG) {
                        currentFile?.delete()
                    }
                }
            }

            CODE_PREFS -> {
                // create temporary JSON file in cache
                val prefs = getPrefs()
                // create default key for vanilla/modded selector. Default is modded
                if (!prefs.all.keys.contains(ISMODDED_KEY)) {
                    prefs.edit {
                        putBoolean(ISMODDED_KEY, true)
                    }
                }
                val nonNullContext = context!!
                val tempPrefsPath = File(nonNullContext.cacheDir, "prefs.json")
                if (tempPrefsPath.exists()) {
                    tempPrefsPath.delete()
                }
                tempPrefsPath.writeText(JSONObject(prefs.all).toString())
                return ParcelFileDescriptor.open(tempPrefsPath, ParcelFileDescriptor.MODE_READ_ONLY)
            }
        }
        return null
    }

    private fun getCurrentZip(): File? {
        if (context == null) {
            return null
        }
        val sharedPrefs =
            context!!.getSharedPreferences(StringKeys.SharedPrefsKey.string, Context.MODE_PRIVATE)
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
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val file = getCurrentZip() ?: return null

        // Standard columns for file metadata
        val columns = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns).apply {
            if (file.exists()) {
                newRow().add(OpenableColumns.DISPLAY_NAME, "latest_save.zip")
                    .add(OpenableColumns.SIZE, file.length())
            }
        }
    }

}