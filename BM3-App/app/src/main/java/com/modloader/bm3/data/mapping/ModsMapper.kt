package com.modloader.bm3.data.mapping

import com.modloader.bm3.data.model.room.ModEntity
import com.modloader.bm3.domain.model.ModModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun ModEntity.toDomain() : ModModel {
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

fun Flow<List<ModEntity>>.toDomain() : Flow<List<ModModel>> {
    return this.map {
        entities -> entities.map { it.toDomain() }
    }
}