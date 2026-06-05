package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.remote.ApiClient
import com.example.gruya.data.remote.dtos.request.CreateProviderProfileRequest
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType


class ProviderRepository {
    suspend fun create(serviceType: ServiceType,description: String, location: Location): Boolean{
        val request = CreateProviderProfileRequest(
            serviceType = serviceType,
            description = description,
            location = location
        )
        val response = ApiClient.providerService.create(request)
        Log.d("API",response.toString())
        return response.isSuccessful
    }
}