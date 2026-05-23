package com.example.gruya.network.service

import com.example.gruya.network.Constants.AUTH_PATH
import com.example.gruya.network.dtos.request.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService{
    @POST(AUTH_PATH + "/login")
    suspend fun login(@Body request: LoginRequest)
}