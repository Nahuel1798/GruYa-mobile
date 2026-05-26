package com.example.gruya.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java


object ApiClient {
    private const val BASE_URL = Constants.BASE_URL

    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val authService: AuthService = retrofit.create(AuthService::class.java)
    }