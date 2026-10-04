package com.modloader.bm3.domain.interfaces.modManager

import com.modloader.bm3.domain.model.DownloadInfoModel
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ModManager {

    suspend fun downloadMod(
        outDir: File,
        modUrl: String,
        outFileName: String
    ): Flow<DownloadInfoModel>
//    suspend fun removeMod(
//        modsDir: File, fileName: String
//    )

//    suspend fun getDownloadingMods(): List<DownloadInfoModel>

    suspend fun clearFailedDownload(id: Int)

//    suspend fun disableMod(modsDir: File, fileName: String)

}