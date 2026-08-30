package com.modloader.bm3.ui.model

data class Mod(
    val id: Int,
    val title: String,
    val description: String?,
    val url : String,
    val isInstalled : Boolean,
    val isDisabled : Boolean,
    val modVersion : String,
    val requiresSteamodded : Boolean,
    val requiresTalisman : Boolean,
    val thumbnail: String?
)
