package com.example.gruya.data.repository

import com.example.gruya.data.local.dao.PendingAssistanceDao
import com.example.gruya.data.local.dao.FakePendingAssistanceDao
import com.example.gruya.data.local.entity.PendingAssistanceEntity
import com.example.gruya.data.local.entity.SyncStatus
import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.data.remote.dtos.response.AssistanceRouteResponse
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.remote.dtos.response.TripStartedResponse
import com.example.gruya.data.service.AssistanceService
import com.example.gruya.data.sync.SyncHandler
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AssistanceRepositoryOfflineTest {

    @Test
    fun `createOffline queues request with PENDING status`() = runBlocking {
        val dao = FakePendingAssistanceDao()
        val service = FakeAssistanceService()
        val scheduler = FakeSyncScheduler()
        val repo = AssistanceRepository(service, dao, scheduler)

        val request = CreateAssistanceRequest(
            serviceType = ServiceType.AUXILIO,
            issueType = IssueType.NEUMATICO_PINCHADO,
            vehicleId = 1,
            origin = Location(-34.0, -58.0),
            destination = Location(-34.5, -58.5)
        )

        repo.createOffline(request)

        val all = dao.readPending()
        assertEquals(1, all.size)
        assertEquals(SyncStatus.PENDING.name, all[0].status)
    }

    @Test
    fun `createOffline returns Queued with the pending ID`() = runBlocking {
        val dao = FakePendingAssistanceDao()
        val service = FakeAssistanceService()
        val scheduler = FakeSyncScheduler()
        val repo = AssistanceRepository(service, dao, scheduler)

        val request = CreateAssistanceRequest(
            serviceType = ServiceType.AUXILIO,
            issueType = IssueType.NEUMATICO_PINCHADO,
            vehicleId = 1,
            origin = Location(-34.0, -58.0),
            destination = Location(-34.5, -58.5)
        )

        val outcome = repo.createOffline(request)

        assertTrue(outcome is AssistanceRepository.QueueAssistanceOutcome.Queued)
        val queued = outcome as AssistanceRepository.QueueAssistanceOutcome.Queued
        assertTrue(queued.pendingId > 0)
    }

    @Test
    fun `createOffline sets destLat and destLng from request destination`() = runBlocking {
        val dao = FakePendingAssistanceDao()
        val service = FakeAssistanceService()
        val scheduler = FakeSyncScheduler()
        val repo = AssistanceRepository(service, dao, scheduler)

        val request = CreateAssistanceRequest(
            serviceType = ServiceType.AUXILIO,
            issueType = IssueType.NEUMATICO_PINCHADO,
            vehicleId = 1,
            origin = Location(-34.0, -58.0),
            destination = Location(-34.6037, -58.3816)
        )

        val outcome = repo.createOffline(request)
        val queued = outcome as AssistanceRepository.QueueAssistanceOutcome.Queued

        val stored = dao.getById(queued.pendingId)
        assertNotNull(stored)
        assertEquals(-34.6037, stored!!.destLat, 0.001)
        assertEquals(-58.3816, stored.destLng, 0.001)
    }

    @Test
    fun `createOffline enqueues WorkManager`() = runBlocking {
        val dao = FakePendingAssistanceDao()
        val service = FakeAssistanceService()
        val scheduler = FakeSyncScheduler()
        val repo = AssistanceRepository(service, dao, scheduler)

        val request = CreateAssistanceRequest(
            serviceType = ServiceType.AUXILIO,
            issueType = IssueType.NEUMATICO_PINCHADO,
            vehicleId = 1,
            origin = Location(-34.0, -58.0),
            destination = Location(-34.5, -58.5)
        )

        repo.createOffline(request)
        assertTrue(scheduler.enqueueSyncCalled)
    }

    @Test
    fun `syncPendingAssistances marks items as SYNCED on success`() = runBlocking {
        val dao = FakePendingAssistanceDao()
        val service = FakeAssistanceService()
        service.shouldSucceed = true
        val scheduler = FakeSyncScheduler()
        val repo = AssistanceRepository(service, dao, scheduler)

        val entity = PendingAssistanceEntity(
            serviceType = "AUXILIO",
            issueType = "NEUMATICO_PINCHADO",
            vehicleId = 1,
            originLat = -34.0,
            originLng = -58.0,
            destLat = -34.5,
            destLng = -58.5,
            capturedAt = System.currentTimeMillis(),
            status = SyncStatus.PENDING.name
        )
        val id = dao.insert(entity)

        val result = repo.syncPendingAssistances()

        assertTrue(result.isSuccess)
        val updated = dao.getById(id)
        assertNotNull(updated)
        assertEquals(SyncStatus.SYNCED.name, updated!!.status)
    }
}

/**
 * Fake [AssistanceService] that returns configurable responses.
 * Only [create] is implemented for tests; other methods throw.
 */
class FakeAssistanceService : AssistanceService {

    var shouldSucceed: Boolean = true
    var capturedRequest: CreateAssistanceRequest? = null

    override suspend fun create(request: CreateAssistanceRequest): Response<AssistanceResponse> {
        capturedRequest = request
        return if (shouldSucceed) {
            @Suppress("UNCHECKED_CAST")
            Response.success(null as AssistanceResponse?)
        } else {
            Response.error(500, "error".toResponseBody("text/plain".toMediaTypeOrNull()))
        }
    }

    override suspend fun getLocation(
        latitude: Double,
        longitude: Double
    ): List<ProviderLocationResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun getNearbyAssistances(rangeKm: Double): List<NearbyAssistanceResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun getUserAssistances(): List<AssistanceResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun getDetailsAssistances(id: Int): Response<AssistanceResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun getActiveAssistances(): Response<AssistanceResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun cancelAssistances(): Response<Unit> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun startTrip(id: Int): Response<TripStartedResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun getRoute(assistanceId: Int): AssistanceRouteResponse =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun arriveAtOrigin(id: Int): Response<Unit> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun headToDestination(id: Int): Response<Unit> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun complete(id: Int): Response<Unit> =
        throw UnsupportedOperationException("Not used in this test")
}

/**
 * Fake [SyncHandler] that records whether [enqueueSync] was called.
 * Implements the interface directly — no [WorkManager] dependency needed.
 */
class FakeSyncScheduler : SyncHandler {
    var enqueueSyncCalled = false

    override fun enqueueSync() {
        enqueueSyncCalled = true
    }

    override fun enqueueSyncOnReconnect() {
        enqueueSync()
    }
}
