package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.response.FuelStationDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FuelStationService {
    @GET(Constants.FUELSTATION_PATH)
    suspend fun getFuelStations(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int = 5000
    ): List<FuelStationDto>
}