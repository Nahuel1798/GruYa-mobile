package com.example.gruya.data.remote

import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService{
    @POST(Constants.AUTH_PATH + "/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}