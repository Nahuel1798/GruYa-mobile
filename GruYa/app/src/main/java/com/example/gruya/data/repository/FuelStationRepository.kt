package com.example.gruya.data.repository

import com.example.gruya.data.remote.dtos.response.FuelStationDto
import com.example.gruya.data.service.FuelStationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FuelStationRepository @Inject constructor(private val FuelStationService: FuelStationService) {
    suspend fun getNearbyStations(
        lat: Double,
        lon: Double
    ): List<FuelStationDto> {
        return FuelStationService.getFuelStations(lat,lon)
    }
}