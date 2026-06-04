package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VehicleService {
    // Crear Auto
    @POST(Constants.AUTH_PATH + "/vehicles/create")
    suspend fun create(@Body request: CreateVehicleRequest) : Response

    // Traer Autos
    @GET(Constants.AUTH_PATH + "/vehicles/")
    suspend fun vehicles()
}