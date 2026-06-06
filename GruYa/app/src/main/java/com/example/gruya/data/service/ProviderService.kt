package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateProviderProfileRequest
import com.example.gruya.data.remote.dtos.response.ProviderProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ProviderService {
    // Crear Profile provider
    @POST(Constants.PROVIDER_PROFILE_PATH)
    suspend fun create(
        @Header("Authorization") token: String,
        @Body request: CreateProviderProfileRequest): Response<ProviderProfileResponse>
    //

}