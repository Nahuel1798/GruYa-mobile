package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.request.UpdateVehicleRequest
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VehicleService {
    @POST(Constants.VEHICLES_PATH)
    suspend fun create(
        @Body request: CreateVehicleRequest
    ): Response<VehicleResponse>

    @GET(Constants.VEHICLES_PATH)
    suspend fun getAll(): Response<List<VehicleResponse>>

    @GET(Constants.VEHICLES_PATH + "/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): Response<VehicleResponse>

    @PUT(Constants.VEHICLES_PATH + "/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body request: UpdateVehicleRequest
    ): Response<VehicleResponse>

    @DELETE(Constants.VEHICLES_PATH + "/{id}")
    suspend fun delete(
        @Path("id") id: Int
    ): Response<Unit>
}