package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.SessionManager
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.ApiClient
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.request.UpdateVehicleRequest
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import com.example.gruya.domain.model.Vehicle

class VehicleRepository {
    val vehicleService = ApiClient.vehicleService

    suspend fun listAll(token: String): List<Vehicle>{
        val response = vehicleService.getAll("Bearer $token")
        if (response.isSuccessful) return response.body()!!.toDomain()
        else Log.d("API_ERROR", "${response.code().toString()} \n ${response.message()}")
        return emptyList()
    }

    suspend fun getById(token: String, id: Int): Vehicle? {
        val response = vehicleService.getById("Bearer $token", id)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun create(token: String, request: CreateVehicleRequest): Vehicle? {
        val response = vehicleService.create("Bearer $token", request)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun update(token: String, id: Int, request: UpdateVehicleRequest): Vehicle? {
        val response = vehicleService.update("Bearer $token", id, request)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun delete(token: String, id: Int): Boolean {
        val response = vehicleService.delete("Bearer $token", id)
        if (response.isSuccessful) return true
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return false
    }
}