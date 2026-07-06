package com.example.gruya.data.repository

import android.content.ContextWrapper
import com.example.gruya.data.local.dao.FakeVehicleCacheDao
import com.example.gruya.data.local.dao.VehicleCacheDao
import com.example.gruya.data.local.entity.VehicleCacheEntity
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import com.example.gruya.data.service.VehicleService
import com.example.gruya.domain.model.VehicleType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class VehicleRepositoryCacheTest {

    @Test
    fun `listAll caches vehicles on success`() = runBlocking {
        val service = FakeVehicleService()
        val cacheDao = FakeVehicleCacheDao()
        val repo = VehicleRepository(service, cacheDao, ContextWrapper(null))

        service.getAllResult = Response.success(
            listOf(
                VehicleResponse(
                    id = 1,
                    type = VehicleType.AUTO,
                    licensePlate = "ABC123",
                    brand = "Toyota",
                    model = "Corolla",
                    insurance = "Full",
                    color = "Red",
                    imageUrl = null
                ),
                VehicleResponse(
                    id = 2,
                    type = VehicleType.CAMIONETA,
                    licensePlate = "XYZ789",
                    brand = "Ford",
                    model = "Ranger",
                    insurance = "Basic",
                    color = "White",
                    imageUrl = null
                )
            )
        )

        val vehicles = repo.listAll()

        assertEquals(2, vehicles.size)
        assertEquals(2, cacheDao.count())
        val cached = cacheDao.getById(1)
        assertEquals("Toyota", cached!!.brand)
    }

    @Test
    fun `getCachedVehicles delegates to cacheDao`() = runBlocking {
        val service = FakeVehicleService()
        val cacheDao = FakeVehicleCacheDao()
        val repo = VehicleRepository(service, cacheDao, ContextWrapper(null))

        cacheDao.upsertAll(
            listOf(
                VehicleCacheEntity(
                    id = 1, type = "AUTO", licensePlate = "ABC123",
                    brand = "Toyota", model = "Corolla", insurance = "Full", color = "Red"
                )
            )
        )

        val cached = repo.getCachedVehicles().first()
        assertEquals(1, cached.size)
        assertEquals("Toyota", cached[0].brand)
    }
}

/**
 * Fake [VehicleService] that returns configurable responses for [getAll].
 * Other methods throw [UnsupportedOperationException].
 */
class FakeVehicleService : VehicleService {

    var getAllResult: Response<List<VehicleResponse>> =
        Response.success(emptyList())

    override suspend fun getAll(): Response<List<VehicleResponse>> = getAllResult

    override suspend fun create(
        type: RequestBody,
        licensePlate: RequestBody,
        brand: RequestBody,
        model: RequestBody,
        insurance: RequestBody,
        color: RequestBody,
        image: MultipartBody.Part?
    ): Response<VehicleResponse> = throw UnsupportedOperationException("Not used in this test")

    override suspend fun getById(id: Int): Response<VehicleResponse> =
        throw UnsupportedOperationException("Not used in this test")

    override suspend fun update(
        id: Int,
        type: RequestBody,
        licensePlate: RequestBody,
        brand: RequestBody,
        model: RequestBody,
        insurance: RequestBody,
        color: RequestBody,
        image: MultipartBody.Part?
    ): Response<VehicleResponse> = throw UnsupportedOperationException("Not used in this test")

    override suspend fun delete(id: Int): Response<Unit> =
        throw UnsupportedOperationException("Not used in this test")
}
