package com.example.gruya.data.local.dao

import com.example.gruya.data.local.entity.VehicleCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleCacheDaoTest {

    private val dao = FakeVehicleCacheDao()

    @Test
    fun `insertAll and getAll`() = runBlocking {
        val vehicles = listOf(
            VehicleCacheEntity(
                id = 1, type = "AUTO", licensePlate = "ABC123",
                brand = "Toyota", model = "Corolla", insurance = "Full", color = "Red"
            ),
            VehicleCacheEntity(
                id = 2, type = "CAMIONETA", licensePlate = "XYZ789",
                brand = "Ford", model = "Ranger", insurance = "Basic", color = "White"
            )
        )
        dao.upsertAll(vehicles)

        val all = dao.getAll().first()
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == 1 && it.brand == "Toyota" })
        assertTrue(all.any { it.id == 2 && it.brand == "Ford" })
    }

    @Test
    fun `upsertAll replaces existing`() = runBlocking {
        dao.upsertAll(
            listOf(
                VehicleCacheEntity(
                    id = 1, type = "AUTO", licensePlate = "ABC123",
                    brand = "Toyota", model = "Corolla", insurance = "Full", color = "Red"
                )
            )
        )

        // Upsert with same ID but different data
        dao.upsertAll(
            listOf(
                VehicleCacheEntity(
                    id = 1, type = "MOTO", licensePlate = "NEW999",
                    brand = "Honda", model = "CBR", insurance = "Premium", color = "Black"
                )
            )
        )

        val all = dao.getAll().first()
        assertEquals(1, all.size)
        assertEquals("MOTO", all[0].type)
        assertEquals("Honda", all[0].brand)
        assertEquals("NEW999", all[0].licensePlate)
    }

    @Test
    fun `deleteAll clears cache`() = runBlocking {
        dao.upsertAll(
            listOf(
                VehicleCacheEntity(
                    id = 1, type = "AUTO", licensePlate = "ABC123",
                    brand = "Toyota", model = "Corolla", insurance = "Full", color = "Red"
                )
            )
        )
        assertEquals(1, dao.count())

        dao.deleteAll()
        assertEquals(0, dao.count())
        assertTrue(dao.getAll().first().isEmpty())
    }

    @Test
    fun `empty cache returns empty list`() = runBlocking {
        val all = dao.getAll().first()
        assertTrue(all.isEmpty())
    }
}

/**
 * In-memory fake implementation of [VehicleCacheDao].
 * Uses a MutableList for storage, matching Room in-memory semantics.
 */
class FakeVehicleCacheDao : VehicleCacheDao {

    private val store = mutableListOf<VehicleCacheEntity>()

    override suspend fun upsertAll(vehicles: List<VehicleCacheEntity>) {
        for (v in vehicles) {
            val idx = store.indexOfFirst { it.id == v.id }
            if (idx >= 0) store[idx] = v
            else store.add(v)
        }
    }

    override fun getAll(): kotlinx.coroutines.flow.Flow<List<VehicleCacheEntity>> =
        flowOf(store.toList())

    override suspend fun deleteAll() {
        store.clear()
    }

    override suspend fun getById(id: Int): VehicleCacheEntity? =
        store.find { it.id == id }

    override suspend fun count(): Int = store.size
}
