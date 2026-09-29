package com.modloader.bm3.ui.mapping

import com.modloader.bm3.domain.model.DownloadInfoModel
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.ui.model.DownloadInfo
import com.modloader.bm3.ui.model.Mod

fun ModModel.toUIModel(): Mod {
    return Mod(
        id = this.id,
        title = this.title,
        description = this.description,
        url = this.url,
        isInstalled = this.isInstalled,
        isDisabled = this.isDisabled,
        modVersion = this.modVersion,
        requiresTalisman = this.requiresTalisman,
        requiresSteamodded = this.requiresSteamodded,
        thumbnail = this.thumbnail,
    )
}

fun Mod.toDomain(): ModModel {
    return ModModel(
        id = this.id,
        title = this.title,
        description = this.description,
        url = this.url,
        isInstalled = this.isInstalled,
        isDisabled = this.isDisabled,
        modVersion = this.modVersion,
        requiresSteamodded = this.requiresSteamodded,
        requiresTalisman = this.requiresTalisman,
        thumbnail = this.thumbnail
    )
}

fun DownloadInfoModel.toUIModel(): DownloadInfo {
    return DownloadInfo(
        this.id,
        this.url,
        this.title,
        this.speed,
        this.timeRemaining,
        this.progress,
        this.state,
        this.failureReason
    )
}

fun List<ModModel>.toUIModels(): List<Mod> {
    return this.map { it.toUIModel() }
}