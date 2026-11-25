package com.example.alp.di

import android.content.Context
import com.example.alp.data.local.TokenManager
import com.example.alp.worker.WorkManagerScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideWorkManagerScheduler(@ApplicationContext context: Context): WorkManagerScheduler {
        return WorkManagerScheduler(context)
    }
}
