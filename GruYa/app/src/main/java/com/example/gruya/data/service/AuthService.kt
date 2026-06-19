package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.FcmTokenRequest
import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.request.RegisterRequest
import com.example.gruya.data.remote.dtos.request.UpdateUserRequest
import com.example.gruya.data.remote.dtos.response.AuthResponse
import com.example.gruya.data.remote.dtos.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthService{
    // Login
    @POST(Constants.AUTH_PATH + "/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // FCM Token
    @PATCH(Constants.AUTH_PATH + "/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<Unit>

    // Register
    @POST(Constants.AUTH_PATH + "/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    // Profile
    @GET(Constants.AUTH_PATH + "/profile")
    suspend fun profile(): Response<UserResponse>
    // editProfile
    @PUT(value = Constants.AUTH_PATH + "/editprofile")
    suspend fun editprofile(
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    // Logout
    @POST(Constants.AUTH_PATH + "/logout")
    suspend fun logout(): Response<Unit>

    // Validate token
    @GET(Constants.AUTH_PATH + "/validate")
    suspend fun validateToken(): Response<Unit>
}