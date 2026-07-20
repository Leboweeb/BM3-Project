package com.modloader.bm3.data.di

import com.modloader.bm3.data.repository.ModsRepositoryImpl
import com.modloader.bm3.domain.repository.ModsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepository(modsRepositoryImpl: ModsRepositoryImpl) : ModsRepository
}