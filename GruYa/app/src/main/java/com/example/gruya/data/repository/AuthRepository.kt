package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.request.RegisterRequest
import com.example.gruya.data.remote.dtos.request.UpdateUserRequest
import com.example.gruya.data.remote.dtos.response.AuthResponse
import com.example.gruya.data.remote.dtos.response.UserResponse
import com.example.gruya.data.service.AuthService
import com.example.gruya.domain.model.Role
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthService
) {
    suspend fun login(email: String, password: String): Response<AuthResponse> {
        val request = LoginRequest(email, password)
        val response = authService.login(request)
        return response
    }

    suspend fun register(firstname: String, lastname: String, email: String, password: String, phone: String, role: Role): Response<AuthResponse>{
        val request = RegisterRequest(
            firstName = firstname,
            lastName = lastname,
            email = email,
            password = password,
            phone = phone,
            role = role
        )
        val response = authService.register(request)
        Log.d("API",response.toString())
        return response
    }

    suspend fun getProfile(): Result<UserResponse> {
        return try {
            val response = authService.profile()
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

    suspend fun editProfile(
        request: UpdateUserRequest
    ): Result<UserResponse?> {
        return try {
            val response =
                authService.editprofile(
                    request
                )

            if (response.isSuccessful) {
                // Devolvemos el usuario si viene en el cuerpo, o null si no
                Result.success(response.body()?.user)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
