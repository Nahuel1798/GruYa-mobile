package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.remote.ApiClient
import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.request.RegisterRequest
import com.example.gruya.data.remote.dtos.response.AuthResponse
import com.example.gruya.data.remote.dtos.response.UserResponse
import com.example.gruya.domain.model.Role
import retrofit2.Response

class AuthRepository {
    suspend fun login(email: String, password: String): Response<AuthResponse> {
        val request = LoginRequest(email, password)
        val response = ApiClient.authService.login(request)
        return response
    }

    suspend fun register(firsname: String, lastname: String, email: String, password: String, phone: String, role: Role): Boolean{
        val request = RegisterRequest(
            firstName = firsname,
            lastName = lastname,
            email = email,
            password = password,
            phone = phone,
            // HARDCODEADO
            role = Role.USER
        )
        val response = ApiClient.authService.register(request)
        Log.d("API",response.toString())
        return response.isSuccessful
    }

    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            val response = ApiClient.authService.profile("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Cuerpo de respuesta nulo"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}