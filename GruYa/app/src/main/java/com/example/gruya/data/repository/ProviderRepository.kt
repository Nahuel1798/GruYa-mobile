package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreateProviderProfileRequest
import com.example.gruya.data.remote.dtos.request.UpdateProviderProfileRequest
import com.example.gruya.data.remote.dtos.response.ProviderProfileResponse
import com.example.gruya.data.service.ProviderService
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ProviderProfile
import com.example.gruya.domain.model.ServiceType
import javax.inject.Inject


class ProviderRepository @Inject constructor(
    private val providerService: ProviderService
) {
    suspend fun create(serviceType: ServiceType, companyName: String, description: String, location: Location, currentLocation: Location?, address: String, isAvailable: Boolean = true): Boolean{
        val request = CreateProviderProfileRequest(
            serviceType = serviceType,
            companyName = companyName,
            description = description,
            location = location,
            currentLocation = currentLocation,
            address = address,
            isAvailable = isAvailable
        )
        val response = providerService.create(
            request
        )
        if (!response.isSuccessful) {
            Log.e("API", "Error creating profile: ${response.code()} ${response.errorBody()?.string()}")
        }
        Log.d("API",response.toString())
        return response.isSuccessful
    }

    suspend fun getprofile(id: Int): Result<ProviderProfileResponse>{
        return try {
            Result.success(providerService.getprofile(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyProfile(): Result<ProviderProfile?> {
        return try {
            val response = providerService.getMyProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.toDomain())
                } else {
                    Result.success(null)
                }
            } else if (response.code() == 404) {
                Result.success(null)
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: UpdateProviderProfileRequest): Result<ProviderProfile> {
        return try {
            val response = providerService.updateProfile(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.toDomain())
                } else {
                    Result.failure(Exception("Cuerpo de respuesta nulo"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
