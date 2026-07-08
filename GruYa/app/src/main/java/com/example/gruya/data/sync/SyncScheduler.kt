package com.example.gruya.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gruya.connectivity.ConnectivityObserver
import com.example.gruya.data.local.dao.PendingAssistanceDao
import com.example.gruya.data.repository.AssistanceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
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
    @ApplicationContext private val context: Context,
    private val connectivityObserver: ConnectivityObserver,
    private val pendingAssistanceDao: PendingAssistanceDao,
    private val assistanceRepositoryProvider: Provider<AssistanceRepository>,
) : SyncHandler {

    private val workManager by lazy { WorkManager.getInstance(context) }

    companion object {
        const val WORK_NAME = "sync_pending_assistances"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        // ── 1. Eager startup sync ───────────────────────────────────
        // Tries to sync pending items directly at app startup.
        // Falls back to WorkManager if the direct sync fails.
        scope.launch {
            try {
                val count = pendingAssistanceDao.observePendingCount().first()
                if (count > 0) {
                    val result = assistanceRepositoryProvider.get().syncPendingAssistances()
                    if (result.isFailure) {
                        enqueueSync()
                    }
                }
            } catch (_: Exception) {
                enqueueSync()
            }
        }

        // ── 2. Reactive observer ────────────────────────────────────
        // Watches for connectivity restoration while items are pending.
        // Reads DB directly instead of relying on Room Flow emissions,
        // then falls back to WorkManager if the sync fails.
        scope.launch {
            try {
                connectivityObserver.observe()
                    .distinctUntilChanged()
                    .collect { status ->
                        if (status == ConnectivityObserver.Status.Available) {
                            val needsSync = withContext(Dispatchers.IO) {
                                pendingAssistanceDao.readNeedsSync()
                            }
                            if (needsSync.isNotEmpty()) {
                                val result = assistanceRepositoryProvider.get().syncPendingAssistances()
                                if (result.isFailure) {
                                    enqueueSync()
                                }
                            }
                        }
                    }
            } catch (_: Exception) {
                // Observer died — no recovery, but WorkManager handles retries.
            }
        }
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
