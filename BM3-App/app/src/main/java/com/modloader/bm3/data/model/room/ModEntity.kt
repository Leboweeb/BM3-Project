package com.modloader.bm3.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Mods")
data class ModEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val url: String,
    @ColumnInfo(name = "isinstalled")
    val isInstalled: Boolean,
    @ColumnInfo(name = "isdisabled")
    val isDisabled: Boolean,
    @ColumnInfo(name = "modversion")
    val modVersion : String,
    @ColumnInfo(name = "requires_steamodded")
    val requiresSteamodded : Boolean,
    @ColumnInfo(name = "requires_talisman")
    val requiresTalisman: Boolean,
    // BASE64 Encoded string stored in Room(SQLite) database
    val thumbnail: String?
)