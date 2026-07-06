package com.example.gruya.di

import android.content.Context
import androidx.work.WorkManager
import com.example.gruya.data.sync.SyncHandler
import com.example.gruya.data.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkManagerModule {

    @Binds
    @Singleton
    abstract fun bindSyncHandler(syncScheduler: SyncScheduler): SyncHandler

    companion object {

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
