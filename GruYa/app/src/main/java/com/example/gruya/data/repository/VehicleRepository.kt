package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.request.UpdateVehicleRequest
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import com.example.gruya.data.service.VehicleService
import com.example.gruya.domain.model.Vehicle
import javax.inject.Inject

class VehicleRepository @Inject constructor(
    private val vehicleService: VehicleService
) {
    suspend fun listAll(): List<Vehicle>{
        val response = vehicleService.getAll()
        if (response.isSuccessful) return response.body()!!.toDomain()
        else Log.d("API_ERROR", "${response.code().toString()} \n ${response.message()}")
        return emptyList()
    }

    suspend fun getById(id: Int): Vehicle? {
        val response = vehicleService.getById(id)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun create(request: CreateVehicleRequest): Vehicle? {
        val response = vehicleService.create(request)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun update(id: Int, request: UpdateVehicleRequest): Vehicle? {
        val response = vehicleService.update(id, request)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun delete(id: Int): Boolean {
        val response = vehicleService.delete(id)
        if (response.isSuccessful) return true
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return false
    }
}
