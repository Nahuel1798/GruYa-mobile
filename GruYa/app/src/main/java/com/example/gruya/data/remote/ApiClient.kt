package com.example.gruya.data.remote

import com.example.gruya.data.service.AuthService
import com.example.gruya.data.service.HelpRequestService
import com.example.gruya.data.service.ProviderService
import com.example.gruya.data.service.VehicleService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java


object ApiClient {
    private val BASE_URL = Constants.BASE_URL
    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val authService: AuthService = retrofit.create(AuthService::class.java)
    val providerService: ProviderService = retrofit.create(ProviderService::class.java)
    //val helpRequestService: HelpRequestService = retrofit.create(HelpRequestService::class.java)
    val vehicleService: VehicleService = retrofit.create(VehicleService::class.java)
}