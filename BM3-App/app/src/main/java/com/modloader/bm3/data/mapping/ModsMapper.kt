package com.modloader.bm3.data.mapping

import com.ketch.DownloadModel
import com.modloader.bm3.data.model.room.ModEntity
import com.modloader.bm3.domain.model.DownloadInfoModel
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.utils.DownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToLong

fun ModEntity.toDomain(): ModModel {
    return ModModel(
        id = this.id,
        title = this.title,
        description = this.description,
        url = this.url,
        isInstalled = this.isInstalled,
        isDisabled = this.isDisabled,
        modVersion = this.modVersion,
        thumbnail = this.thumbnail,
        requiresTalisman = this.requiresTalisman,
        requiresSteamodded = this.requiresSteamodded
    )
}

fun ModModel.toDataLayer(): ModEntity {
    return ModEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        url = this.url,
        isInstalled = this.isInstalled,
        isDisabled = this.isDisabled,
        modVersion = this.modVersion,
        thumbnail = this.thumbnail,
        requiresTalisman = this.requiresTalisman,
        requiresSteamodded = this.requiresSteamodded
    )
}

fun DownloadModel.toDomain(): DownloadInfoModel {
    val speedBytesPerSecond = if (this.speedInBytePerMs != 0f) {
        this.speedInBytePerMs * 1000
    } else {
        -1f
    }
    val progressRemaining = if (this.speedInBytePerMs != 0.0f) {
        (this.total / speedBytesPerSecond).roundToLong()
    } else {
        -1L
    }
    return DownloadInfoModel(
        this.id,
        this.url,
        this.fileName,
        speedBytesPerSecond,
        timeRemaining = progressRemaining,
        progress = this.progress,
        failureReason = this.failureReason,
        // enum in domain is the same as the one from com.ketch, so we can simply reuse the same INT to map to domain enum values.
        state = DownloadStatus.fromInt(this.status.ordinal)
    )
}

fun Flow<List<ModEntity>>.toDomain(): Flow<List<ModModel>> {
    return this.map { entities ->
        entities.map { it.toDomain() }
    }
}