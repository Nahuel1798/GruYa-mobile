package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.remote.ApiClient
import com.example.gruya.data.remote.dtos.request.LoginRequest
import com.example.gruya.data.remote.dtos.request.RegisterRequest
import com.example.gruya.domain.model.Role

class AuthRepository {
    suspend fun login(email: String, password: String): Boolean{
        val request = LoginRequest(email, password)
        val response = ApiClient.authService.login(request)
        Log.d("API",response.toString())
        return response.isSuccessful
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
}