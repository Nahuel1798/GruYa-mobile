package com.example.gruya.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface extracted for testability — allows fake implementations
 * without a [WorkManager] dependency.
 */
interface SyncHandler {
    fun enqueueSync()
    fun enqueueSyncOnReconnect()
}

@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager
) : SyncHandler {

    companion object {
        const val WORK_NAME = "sync_pending_assistances"
    }

    override fun enqueueSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncAssistanceWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun enqueueSyncOnReconnect() {
        enqueueSync()
    }
}
