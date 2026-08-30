package com.modloader.bm3.ui.mapping

import com.modloader.bm3.domain.model.ModModel
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

fun List<ModModel>.toUIModels() :  List<Mod> {
    return this.map { it.toUIModel() }
}