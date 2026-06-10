package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateProviderProfileRequest
import com.example.gruya.data.remote.dtos.response.ProviderProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProviderService {
    // Crear Profile provider
    @POST(Constants.PROVIDER_PROFILE_PATH)
    suspend fun create(
        @Body request: CreateProviderProfileRequest): Response<ProviderProfileResponse>
    //Traer Profile Provider
    @GET(Constants.PROVIDER_PROFILE_PATH + "/{id}")
    suspend fun getprofile(
        @Path("id") id: Int
    ): ProviderProfileResponse
}