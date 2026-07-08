package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.local.dao.PendingAssistanceDao
import com.example.gruya.data.local.entity.PendingAssistanceEntity
import com.example.gruya.data.local.entity.SyncStatus
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.remote.dtos.response.AssistanceRouteResponse
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.remote.dtos.response.TripStartedResponse
import com.example.gruya.data.service.AssistanceService
import com.example.gruya.data.sync.SyncHandler
import com.example.gruya.domain.model.Assistance
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistanceRepository @Inject constructor(
    private val assistanceService: AssistanceService,
    private val pendingAssistanceDao: PendingAssistanceDao,
    private val syncScheduler: SyncHandler
) {

    sealed interface QueueAssistanceOutcome {
        data class Queued(val pendingId: Long) : QueueAssistanceOutcome
        data class Failed(val error: String) : QueueAssistanceOutcome
    }

    suspend fun createOffline(request: CreateAssistanceRequest): QueueAssistanceOutcome {
        return try {
            val now = System.currentTimeMillis()
            val entity = PendingAssistanceEntity(
                serviceType = request.serviceType.name,
                issueType = request.issueType.name,
                vehicleId = request.vehicleId,
                originLat = request.origin.latitude,
                originLng = request.origin.longitude,
                destLat = request.destination.latitude,
                destLng = request.destination.longitude,
                capturedAt = now,
                status = SyncStatus.PENDING.name
            )
            val id = pendingAssistanceDao.insert(entity)
            syncScheduler.enqueueSync()
            Log.d("AssistanceRepository", "Offline request queued with id=$id")
            QueueAssistanceOutcome.Queued(pendingId = id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("AssistanceRepository", "Failed to queue offline request", e)
            QueueAssistanceOutcome.Failed(error = e.message ?: "Unknown error")
        }
    }

    suspend fun syncPendingAssistances(): Result<Unit> {
        return try {
            val pendingItems = pendingAssistanceDao.readNeedsSync()
            if (pendingItems.isEmpty()) return Result.success(Unit)

            var hasFailure = false

            for (item in pendingItems) {
                try {
                    val request = CreateAssistanceRequest(
                        serviceType = ServiceType.valueOf(item.serviceType),
                        issueType = IssueType.valueOf(item.issueType),
                        vehicleId = item.vehicleId,
                        origin = Location(item.originLat, item.originLng),
                        destination = Location(item.destLat, item.destLng)
                    )

                    val response = assistanceService.create(request)
                    if (response.isSuccessful) {
                        pendingAssistanceDao.deleteById(item.id)
                        Log.d("AssistanceRepository", "Synced and deleted pending assistance id=${item.id}")
                    } else if (response.code() == 401) {
                        val updated = item.copy(
                            status = SyncStatus.NEEDS_REAUTH.name,
                            lastError = "401: Session expired"
                        )
                        pendingAssistanceDao.updateStatus(updated)
                        Log.w("AssistanceRepository", "Sync failed (401) for id=${item.id}, marked NEEDS_REAUTH")
                    } else {
                        // Server responded with an error (4xx, 5xx) — retrying won't change the outcome
                        val updated = item.copy(
                            status = SyncStatus.FAILED.name,
                            lastError = "HTTP ${response.code()}"
                        )
                        pendingAssistanceDao.updateStatus(updated)
                        hasFailure = true
                        Log.w("AssistanceRepository", "Sync failed (HTTP ${response.code()}) for id=${item.id}, marked FAILED")
                    }
                } catch (e: Exception) {
                    val newRetryCount = item.retryCount + 1
                    val newStatus = if (newRetryCount >= 5) {
                        SyncStatus.FAILED.name
                    } else {
                        SyncStatus.PENDING.name
                    }
                    val updated = item.copy(
                        status = newStatus,
                        retryCount = newRetryCount,
                        lastError = e.message
                    )
                    pendingAssistanceDao.updateStatus(updated)
                    hasFailure = true
                    Log.e("AssistanceRepository", "Sync error for id=${item.id}", e)
                }
            }

            return if (hasFailure) {
                Result.failure(Exception("Some items failed to sync"))
            } else {
                Result.success(Unit)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPendingAssistance(id: Long): Result<Unit> {
        return try {
            val item = pendingAssistanceDao.getById(id)
                ?: return Result.failure(Exception("Pending assistance not found"))

            try {
                val request = CreateAssistanceRequest(
                    serviceType = ServiceType.valueOf(item.serviceType),
                    issueType = IssueType.valueOf(item.issueType),
                    vehicleId = item.vehicleId,
                    origin = Location(item.originLat, item.originLng),
                    destination = Location(item.destLat, item.destLng)
                )

                val response = assistanceService.create(request)
                if (response.isSuccessful) {
                    pendingAssistanceDao.deleteById(item.id)
                    Log.d("AssistanceRepository", "Synced and deleted pending assistance id=${item.id}")
                    Result.success(Unit)
                } else if (response.code() == 401) {
                    val updated = item.copy(
                        status = SyncStatus.NEEDS_REAUTH.name,
                        lastError = "401: Session expired"
                    )
                    pendingAssistanceDao.updateStatus(updated)
                    Log.w("AssistanceRepository", "Sync failed (401) for id=${item.id}, marked NEEDS_REAUTH")
                    Result.failure(Exception("Session expired"))
                } else {
                    val updated = item.copy(
                        status = SyncStatus.FAILED.name,
                        lastError = "HTTP ${response.code()}"
                    )
                    pendingAssistanceDao.updateStatus(updated)
                    Log.w("AssistanceRepository", "Sync failed (HTTP ${response.code()}) for id=${item.id}, marked FAILED")
                    Result.failure(Exception("HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                val newRetryCount = item.retryCount + 1
                val newStatus = if (newRetryCount >= 5) {
                    SyncStatus.FAILED.name
                } else {
                    SyncStatus.PENDING.name
                }
                val updated = item.copy(
                    status = newStatus,
                    retryCount = newRetryCount,
                    lastError = e.message
                )
                pendingAssistanceDao.updateStatus(updated)
                Log.e("AssistanceRepository", "Sync error for id=${item.id}", e)
                Result.failure(e)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingById(id: Long): PendingAssistanceEntity? =
        pendingAssistanceDao.getById(id)

    fun observePendingCount(): Flow<Int> =
        pendingAssistanceDao.observePendingCount()

    fun observePendingAssistances(): Flow<List<PendingAssistanceEntity>> =
        pendingAssistanceDao.observeAll()

    suspend fun deletePendingAssistance(id: Long) {
        pendingAssistanceDao.deleteById(id)
    }

    suspend fun create(request: CreateAssistanceRequest): Result<AssistanceResponse?> {
        return try {
            val response = assistanceService.create(request)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Solicitud de auxilio creada: $body")
                Result.success(body)
            } else {
                Result.failure(
                    Exception("${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProviderlocation(
        latitude: Double,
        longitude: Double,
    ): List<ProviderLocationResponse> {
        return assistanceService.getLocation(
            latitude = latitude,
            longitude = longitude,
        )
    }

    suspend fun getNearbyAssistances(
        rangeKm: Double = 20.0
    ): List<NearbyAssistanceResponse> {
        val result = assistanceService.getNearbyAssistances(rangeKm)
        Log.d("AssistanceRepository", "Solicitudes de auxilio cercanas: $result")
        return result
    }
    
    suspend fun getAssistanceDetails(id: Int): Result<AssistanceResponse?> {
        return try {
            val response = assistanceService.getDetailsAssistances(id)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Solicitud de auxilio creada: $body")
                Result.success(body)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Esta asistencia ya no está disponible"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserAssistances(): Result<List<Assistance>> {
        return try {
            val response = assistanceService.getUserAssistances()
            val domain = response.toDomain()
            Log.d("AssistanceRepository", "Solicitudes de auxilio del usuario: $domain")
            Result.success(domain)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener las solicitudes de auxilio", e))
        }
    }


    suspend fun getAssistanceActive(): Result<Assistance?> {
        return try {
            val response = assistanceService.getActiveAssistances()
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Solicitud de auxilio activa: $body")
                Result.success(body?.toDomain())
            } else if (response.code() == 404) {
                // Si es 404, simplemente no hay asistencia activa, no es un error para el usuario
                Log.d("AssistanceRepository", "No hay asistencia activa (404)")
                Result.success(null)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener la solicitud activa", e))
        }
    }

    suspend fun cancelAssistance(): Result<Unit> {
        return try {
            val response = assistanceService.cancelAssistances()
            if (response.isSuccessful) {
                Log.d("AssistanceRepository", "Solicitud de auxilio cancelada")
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error al cancelar la solicitud", e))
        }
    }

    suspend fun startTrip(id: Int): Result<TripStartedResponse?> {
        return try {
            val response = assistanceService.startTrip(id)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Viaje iniciado: $body")
                Result.success(body)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoute(id: Int): Result<AssistanceRouteResponse> {
        return try {
            val response = assistanceService.getRoute(id)
            Log.d("AssistanceRepository", "Ruta obtenida: $response")
            Result.success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun arriveAtOrigin(id: Int): Result<Unit> {
        return try {
            val response = assistanceService.arriveAtOrigin(id)
            if (response.isSuccessful) {
                Log.d("AssistanceRepository", "Llegada al origen registrada para asistencia $id")
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun headToDestination(id: Int): Result<Unit> {
        return try {
            val response = assistanceService.headToDestination(id)
            if (response.isSuccessful) {
                Log.d("AssistanceRepository", "Dirección al destino registrada para asistencia $id")
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeService(id: Int): Result<Unit> {
        return try {
            val response = assistanceService.complete(id)
            if (response.isSuccessful) {
                Log.d("AssistanceRepository", "Servicio completado para asistencia $id")
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
