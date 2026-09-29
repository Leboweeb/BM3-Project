package com.modloader.bm3.data.di

import com.modloader.bm3.data.datasource.remote.GithubApiModManager
import com.modloader.bm3.domain.interfaces.modManager.ModManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class ModManagerModule {

    @Binds
    @Singleton
    abstract fun bindModManager(githubApiModManager: GithubApiModManager): ModManager
}