package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.request.UpdateVehicleRequest
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface VehicleService {
    @Multipart
    @POST(Constants.VEHICLES_PATH)
    suspend fun create(
        @Part("type") type: RequestBody,
        @Part("licensePlate") licensePlate: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("model") model: RequestBody,
        @Part("insurance") insurance: RequestBody,
        @Part("color") color: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<VehicleResponse>

    @GET(Constants.VEHICLES_PATH)
    suspend fun getAll(): Response<List<VehicleResponse>>

    @GET(Constants.VEHICLES_PATH + "/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): Response<VehicleResponse>

    @Multipart
    @PUT(Constants.VEHICLES_PATH + "/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Part("type") type: RequestBody,
        @Part("licensePlate") licensePlate: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("model") model: RequestBody,
        @Part("insurance") insurance: RequestBody,
        @Part("color") color: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<VehicleResponse>

    @DELETE(Constants.VEHICLES_PATH + "/{id}")
    suspend fun delete(
        @Path("id") id: Int
    ): Response<Unit>
}