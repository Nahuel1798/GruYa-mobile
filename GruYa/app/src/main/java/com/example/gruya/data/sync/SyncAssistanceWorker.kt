package com.example.gruya.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gruya.data.repository.AssistanceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class SyncAssistanceWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val repository: AssistanceRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java
        ).assistanceRepository()
    }

    override suspend fun doWork(): Result {
        return repository.syncPendingAssistances().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun assistanceRepository(): AssistanceRepository
    }
}
