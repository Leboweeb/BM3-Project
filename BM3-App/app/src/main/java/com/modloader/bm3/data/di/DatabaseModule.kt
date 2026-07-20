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
    fun provideModsDao(db : ModsDatabase) : ModsDao {
        return db.ModsDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(context: Application) : ModsDatabase {
        return Room.databaseBuilder(
            context = context,
            ModsDatabase::class.java,
            "Mods.db"
        )
            .createFromAsset("database/mods.db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

}