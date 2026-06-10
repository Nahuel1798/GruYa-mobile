package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.remote.dtos.request.CreateProviderProfileRequest
import com.example.gruya.data.remote.dtos.response.ProviderProfileResponse
import com.example.gruya.data.service.ProviderService
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import javax.inject.Inject


class ProviderRepository @Inject constructor(
    private val providerService: ProviderService
) {
    suspend fun create(serviceType: ServiceType, description: String, location: Location): Boolean{
        val request = CreateProviderProfileRequest(
            serviceType = serviceType,
            description = description,
            location = location
        )
        val response = providerService.create(
            request
        )
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

}
