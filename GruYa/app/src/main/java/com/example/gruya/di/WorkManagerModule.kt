package com.example.gruya.di

import com.example.gruya.data.sync.SyncHandler
import com.example.gruya.data.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkManagerModule {

    @Binds
    @Singleton
    abstract fun bindSyncHandler(syncScheduler: SyncScheduler): SyncHandler
}
