package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.remote.ApiClient
import com.example.gruya.data.remote.dtos.request.LoginRequest

class AuthRepository {
    suspend fun login(email: String, password: String): Boolean{
        val request = LoginRequest(email, password)
        val response = ApiClient.authService.login(request)
        Log.d("API",response.toString())
        return response.isSuccessful
    }
}