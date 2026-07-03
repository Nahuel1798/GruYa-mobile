package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.FcmTokenRequest
import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.request.RegisterRequest
import com.example.gruya.data.remote.dtos.request.UpdatePasswordRequest
import com.example.gruya.data.remote.dtos.request.UpdateUserRequest
import com.example.gruya.data.remote.dtos.response.AuthResponse
import com.example.gruya.data.remote.dtos.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

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

    @Multipart
    @POST(Constants.AUTH_PATH + "/avatar")
    suspend fun updateAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<UserResponse>

    // Logout
    @POST(Constants.AUTH_PATH + "/logout")
    suspend fun logout(): Response<Unit>

    // Validate token
    @GET(Constants.AUTH_PATH + "/validate")
    suspend fun validateToken(): Response<Unit>

    @PATCH(Constants.AUTH_PATH + "/password")
    suspend fun resetpassword(
        @Body request: UpdatePasswordRequest
    ): Response<Unit>
}