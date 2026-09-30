package com.modloader.bm3.data.datasource.remote

import android.util.Log
import com.ketch.Ketch
import com.modloader.bm3.data.mapping.toDomain
import com.modloader.bm3.domain.interfaces.modManager.ModManager
import com.modloader.bm3.domain.model.DownloadInfoModel
import com.modloader.bm3.utils.BM3_DATA_GITHUBMODMANAGER_TAG
import com.modloader.bm3.utils.INSTALLED_MODS_FILE
import com.modloader.bm3.utils.REMAINING_RATELIMIT_GITHUB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class GithubApiModManager
@Inject constructor(
    private val ketchInstance: Ketch
) : ModManager {

    companion object {
        private var rateLimitUsed = 0
        private const val RATE_LIMIT_MAX = 60
    }


    // Download mods in real app to cache/mods/* !!!
    // returns int that represents % of completion of download
    suspend fun downloadMod_(
        modsDir: File,
        modUrl: String,
        outFileName: String
    ): Flow<Int> {
        // DOWNLOADED MOD MUST BE SAME AS TITLE IN DB!!!
        val outputFile = File(modsDir, "$outFileName.zip")
        val result = MutableStateFlow(0)
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request =
                Request.Builder().url("https://api.github.com/repos/$modUrl/zipball").build()
            if (rateLimitUsed >= RATE_LIMIT_MAX) {
                Log.e(BM3_DATA_GITHUBMODMANAGER_TAG, "Too many requests, try again later.")
                return@withContext
            }
            val networkCall = client.newCall(request)
            val response = runInterruptible { networkCall.execute() }
            rateLimitUsed = (response.headers[REMAINING_RATELIMIT_GITHUB] ?: "0").toInt()
            if (!response.isSuccessful) {
                Log.e(
                    BM3_DATA_GITHUBMODMANAGER_TAG,
                    "Download failed with HTTP status ${response.code}"
                )
                return@withContext
            }
            val body = response.body
            if (body == null) {
                Log.e(
                    BM3_DATA_GITHUBMODMANAGER_TAG,
                    "Attempted to download mod $outFileName from URL $modUrl but got null body!"
                )
                return@withContext
            }

            body.byteStream().use { inputStream ->
                val bodyLength = body.contentLength()
                var bytesCopied = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesToRead = inputStream.read(buffer)
                outputFile.outputStream().use { outputStream ->
                    while (bytesToRead >= 0) {
                        outputStream.write(buffer, 0, bytesToRead)
                        bytesCopied += bytesToRead

                        result.value = ((bytesCopied / bodyLength) * 100).toInt()
                        bytesToRead = inputStream.read(buffer)
                    }
                }
            }
        }
        return result.asStateFlow()
    }

    override suspend fun downloadMod(
        outDir: File,
        modUrl: String,
        outFileName: String
    ): Flow<DownloadInfoModel> {
        val file = File(outDir, "$outFileName.zip")
        if (!outDir.exists()) {
            // ensures mods dir file is created first
            withContext(Dispatchers.IO) {
                outDir.mkdirs()
            }
        }

        val downloadID =
            ketchInstance.download(
                url = modUrl, path = outDir.toString(), fileName = file.name
            )
        return ketchInstance.observeDownloadById(downloadID).filterNotNull().map {
            it.toDomain()
        }

    }

    override suspend fun removeMod(modsDir: File, fileName: String) {
        withContext(context = Dispatchers.IO) {
            val outputFile = File(modsDir, fileName)
            if (outputFile.exists()) {
                outputFile.delete()
            }
        }
    }

    override suspend fun getDownloadingMods(): List<DownloadInfoModel> {
//        return ketchInstance.observeDownloads().map { models ->
//            models.map { it.toDomain() }
//        }
        return ketchInstance.getAllDownloads().map { it.toDomain() }
    }

    override suspend fun clearFailedDownload(id: Int) {
        ketchInstance.clearDb(id, true)
    }

    suspend fun zipMods(modsDir: File, installedModNames: List<String>) {
        withContext(context = Dispatchers.IO) {
            // get all currently installed mods and deploy as zip folder in cache, use fixed name because there are a lot of zip files in cache apparently
            // also assume mods are in cache/mods
            val destinationFile = File(modsDir, "$INSTALLED_MODS_FILE.zip")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            ZipOutputStream(destinationFile.outputStream().buffered()).use { zipOutputStream ->
                modsDir.walkTopDown()
                    .filter { file -> file.nameWithoutExtension in installedModNames }
                    .forEach { file ->
                        // prevent file from zipping itself if in same directory as files.
                        // also, canonical path resolves symlink edge case
                        if (file.isDirectory || (file.canonicalPath == destinationFile.canonicalPath)) {
                            return@forEach
                        }
                        val zipEntryPath = file.relativeTo(modsDir).path.replace('\\', '/')
                        zipOutputStream.putNextEntry(ZipEntry(zipEntryPath))
                        file.inputStream().buffered().use { inputStream ->
                            inputStream.copyTo(zipOutputStream)
                        }
                    }
            }
        }
    }
}
