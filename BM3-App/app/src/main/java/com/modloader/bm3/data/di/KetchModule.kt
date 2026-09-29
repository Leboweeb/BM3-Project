package com.modloader.bm3.data.di

import android.app.Application
import com.ketch.Ketch
import com.ketch.NotificationConfig
import com.modloader.bm3.R
import com.modloader.bm3.utils.GithubApiRateLimitInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object KetchModule {

    @Provides
    @Singleton
    fun provideKetchManager(context: Application): Ketch {
        return Ketch.builder()
            .setNotificationConfig(
                NotificationConfig(
                    enabled = true,
                    smallIcon = R.drawable.ic_launcher_foreground
                )
            )
            .setOkHttpClient(
                OkHttpClient().newBuilder()
                    .addNetworkInterceptor(GithubApiRateLimitInterceptor(context)).build()

            )
            .enableLogs(true)
            .build(context)
    }
}