package com.modloader.bm3.data.datasource.remote

import com.ketch.Ketch
import com.modloader.bm3.data.mapping.toDomain
import com.modloader.bm3.domain.interfaces.modManager.ModManager
import com.modloader.bm3.domain.model.DownloadInfoModel
import com.modloader.bm3.utils.INSTALLED_MODS_FILE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.IOException
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

    override suspend fun downloadMod(
        outDir: File,
        modUrl: String,
        outFileName: String
    ): Flow<DownloadInfoModel> {
        return withContext(Dispatchers.IO) {
            val file = File(outDir, "$outFileName.zip")
            if (!outDir.exists()) {
                // ensures mods dir file is created first
                outDir.mkdirs()
            }
            if (rateLimitUsed >= RATE_LIMIT_MAX) {
                throw IOException("Too many requests to github API! Please try again in an hour.")
            }
            val downloadID =
                ketchInstance.download(
                    url = modUrl, path = outDir.toString(), fileName = file.name
                )
            rateLimitUsed += 1
            return@withContext ketchInstance.observeDownloadById(downloadID).filterNotNull().map {
                it.toDomain()
            }
        }

    }

//    override suspend fun getDownloadingMods(): List<DownloadInfoModel> {
//        return ketchInstance.getAllDownloads().map { it.toDomain() }
//    }

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
