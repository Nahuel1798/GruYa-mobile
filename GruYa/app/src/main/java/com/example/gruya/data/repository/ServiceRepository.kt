package com.example.gruya.data.repository

import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.service.ServiceService
import com.example.gruya.di.NetworkModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val serviceService: ServiceService){

    suspend fun getProviderlocation(
        latitude: Double,
        longitude: Double,
    ): List<ProviderLocationResponse> {
        return serviceService.getLocation(
            latitude = latitude,
            longitude = longitude,
        )
    }
}