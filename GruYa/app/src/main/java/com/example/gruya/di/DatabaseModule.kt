package com.example.gruya.di

import android.content.Context
import androidx.room.Room
import com.example.gruya.data.local.AppDatabase
import com.example.gruya.data.local.dao.PendingAssistanceDao
import com.example.gruya.data.local.dao.VehicleCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gruya-db"
        ).build()

    @Provides
    @Singleton
    fun providePendingAssistanceDao(database: AppDatabase): PendingAssistanceDao =
        database.pendingAssistanceDao()

    @Provides
    @Singleton
    fun provideVehicleCacheDao(database: AppDatabase): VehicleCacheDao =
        database.vehicleCacheDao()
}
