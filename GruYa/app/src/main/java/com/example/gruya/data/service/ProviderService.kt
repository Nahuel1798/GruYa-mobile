package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateProviderProfileRequest
import com.example.gruya.data.remote.dtos.request.UpdateProviderProfileRequest
import com.example.gruya.data.remote.dtos.response.ProviderProfileResponse
import com.example.gruya.domain.model.Location
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProviderService {
    // Crear Profile provider
    @POST(Constants.PROVIDER_PROFILE_PATH)
    suspend fun create(
        @Body request: CreateProviderProfileRequest
    ): Response<ProviderProfileResponse>

    // Traer Profile Provider
    @GET(Constants.PROVIDER_PROFILE_PATH + "{id}")
    suspend fun getprofile(
        @Path("id") id: Int
    ): ProviderProfileResponse

    // Traer mi propio perfil de provider
    @GET(Constants.PROVIDER_PROFILE_PATH + "me")
    suspend fun getMyProfile(): Response<ProviderProfileResponse>

    // Actualizar perfil de provider
    @PUT(Constants.PROVIDER_PROFILE_PATH)
    suspend fun updateProfile(
        @Body request: UpdateProviderProfileRequest
    ): Response<ProviderProfileResponse>

    @PATCH("${Constants.PROVIDER_PROFILE_PATH}location")
    suspend fun updateProviderLocation(@Body location: Location): Response<Unit>
}