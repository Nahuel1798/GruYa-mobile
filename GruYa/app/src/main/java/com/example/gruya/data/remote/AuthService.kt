package com.example.gruya.data.remote

import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.request.RegisterRequest
import com.example.gruya.data.remote.dtos.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService{
    // Login
    @POST(Constants.AUTH_PATH + "/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    // Register
    @POST(Constants.AUTH_PATH + "/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}