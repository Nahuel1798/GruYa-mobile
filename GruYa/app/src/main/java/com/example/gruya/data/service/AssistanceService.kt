package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.remote.dtos.response.AssistanceRequestResponse
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AssistanceService {
    @POST("${Constants.ASSISTANCE_PATH}/request")
    suspend fun create(@Body request: CreateAssistanceRequest): Response<AssistanceRequestResponse>

    @GET("${Constants.ASSISTANCE_PATH}/providers-nearby")
    suspend fun getLocation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): List<ProviderLocationResponse>

    @GET("${Constants.ASSISTANCE_PATH}/assistance-nearby")
    suspend fun getNearbyAssistances(
        @Query("rangeKm") rangeKm: Double = 20.0
    ): List<NearbyAssistanceResponse>
}
