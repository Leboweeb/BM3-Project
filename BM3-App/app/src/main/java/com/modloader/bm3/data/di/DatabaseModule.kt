package com.modloader.bm3.data.di

import android.app.Application
import androidx.room.Room
import com.modloader.bm3.data.datasource.local.ModsDao
import com.modloader.bm3.data.datasource.local.ModsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideModsDao(db: ModsDatabase): ModsDao {
        return db.ModsDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(context: Application): ModsDatabase {
        val dbName = "Mods.db"
        var dbInstance = Room.databaseBuilder(
            context = context,
            ModsDatabase::class.java,
            dbName
        )
            .fallbackToDestructiveMigration(false)
        if (!context.getDatabasePath(dbName).exists()) {
            dbInstance = dbInstance.createFromAsset("database/mods.db")
        }
        return dbInstance.build()
    }

}