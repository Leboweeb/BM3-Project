package com.modloader.bm3.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModModel (
    val id: Int,
    val title: String,
    val description: String?,
    val url : String,
    val isInstalled : Boolean,
    val isDisabled : Boolean,
    val modVersion : String,
    val requiresSteamodded : Boolean,
    val requiresTalisman : Boolean,
    // BASE64 Encoded string stored in Room(SQLite) database
    val thumbnail: String?
)
