package com.example.gruya.data.local.dao

import com.example.gruya.data.local.entity.PendingAssistanceEntity
import com.example.gruya.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PendingAssistanceDaoTest {

    private val dao = FakePendingAssistanceDao()

    @Test
    fun `insert and read back`() = runBlocking {
        val entity = createEntity()
        val id = dao.insert(entity)

        val loaded = dao.getById(id)
        assertNotNull(loaded)
        assertEquals(id, loaded?.id)
        assertEquals("AUXILIO", loaded?.serviceType)
        assertEquals("NEUMATICO_PINCHADO", loaded?.issueType)
        assertEquals(1, loaded?.vehicleId)
        assertEquals(-34.0, loaded!!.originLat, 0.001)
        assertEquals(-58.0, loaded!!.originLng, 0.001)
        assertEquals(-34.5, loaded!!.destLat, 0.001)
        assertEquals(-58.5, loaded!!.destLng, 0.001)
    }

    @Test
    fun `getPending returns only PENDING items`() = runBlocking {
        dao.insert(createEntity(status = SyncStatus.PENDING.name))
        dao.insert(createEntity(status = SyncStatus.SYNCED.name, vehicleId = 2))
        dao.insert(createEntity(status = SyncStatus.FAILED.name, vehicleId = 3))

        val pending = dao.readPending()
        assertEquals(1, pending.size)
        assertEquals(SyncStatus.PENDING.name, pending[0].status)
        assertEquals(1, pending[0].vehicleId)
    }

    @Test
    fun `updateStatus`() = runBlocking {
        val id = dao.insert(createEntity(status = SyncStatus.PENDING.name))
        val updated = dao.getById(id)!!.copy(
            status = SyncStatus.SYNCED.name,
            retryCount = 1,
            lastError = null
        )
        dao.updateStatus(updated)

        val reloaded = dao.getById(id)
        assertEquals(SyncStatus.SYNCED.name, reloaded?.status)
    }

    @Test
    fun `incrementRetry`() = runBlocking {
        val id = dao.insert(createEntity(status = SyncStatus.PENDING.name, retryCount = 0))
        val loaded = dao.getById(id)!!
        dao.updateStatus(loaded.copy(retryCount = loaded.retryCount + 1))

        val reloaded = dao.getById(id)
        assertEquals(1, reloaded?.retryCount)
    }

    @Test
    fun `observePendingCount`() = runBlocking {
        dao.insert(createEntity())
        dao.insert(createEntity(vehicleId = 2))
        dao.insert(createEntity(vehicleId = 3))

        val count = dao.observePendingCount().first()
        assertEquals(3, count)
    }

    @Test
    fun `deleteById`() = runBlocking {
        val id = dao.insert(createEntity())
        assertNotNull(dao.getById(id))

        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    private fun createEntity(
        status: String = SyncStatus.PENDING.name,
        vehicleId: Int = 1,
        retryCount: Int = 0
    ) = PendingAssistanceEntity(
        serviceType = "AUXILIO",
        issueType = "NEUMATICO_PINCHADO",
        vehicleId = vehicleId,
        originLat = -34.0,
        originLng = -58.0,
        destLat = -34.5,
        destLng = -58.5,
        capturedAt = System.currentTimeMillis(),
        status = status,
        retryCount = retryCount
    )
}

/**
 * In-memory fake implementation of [PendingAssistanceDao].
 * Simulates Room DAO behavior without an Android context.
 */
class FakePendingAssistanceDao : PendingAssistanceDao {

    private val store = mutableListOf<PendingAssistanceEntity>()
    private var nextId = 1L

    override suspend fun insert(entity: PendingAssistanceEntity): Long {
        val newEntity = entity.copy(id = nextId)
        store.add(newEntity)
        return nextId++
    }

    override suspend fun updateStatus(entity: PendingAssistanceEntity) {
        val idx = store.indexOfFirst { it.id == entity.id }
        if (idx >= 0) store[idx] = entity
    }

    override suspend fun readPending(): List<PendingAssistanceEntity> =
        store.filter { it.status == SyncStatus.PENDING.name }
            .sortedBy { it.capturedAt }

    override suspend fun getById(id: Long): PendingAssistanceEntity? =
        store.find { it.id == id }

    override suspend fun deleteById(id: Long) {
        store.removeAll { it.id == id }
    }

    override fun observePendingCount(): kotlinx.coroutines.flow.Flow<Int> =
        flow { emit(store.size) }

    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<PendingAssistanceEntity>> =
        flow { emit(store.toList().sortedByDescending { it.capturedAt }) }
}
