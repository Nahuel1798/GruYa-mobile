package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ServiceService {
    //Traer las ubicaciones de los servicios
    @GET("${Constants.SERVICE_PATH}/providers-nearby")
    suspend fun getLocation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): List<ProviderLocationResponse>

}