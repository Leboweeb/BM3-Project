package com.modloader.bm3.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.modloader.bm3.data.model.room.ModEntity

@Database(entities = [ModEntity::class], version = 1 )
abstract class ModsDatabase : RoomDatabase() {
    abstract fun ModsDao() : ModsDao
}