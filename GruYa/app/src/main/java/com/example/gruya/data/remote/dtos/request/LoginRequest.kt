package com.example.gruya.data.remote.dtos.request

data class LoginRequest(
    val email : String,
    val password : String,
    val fcmToken: String? = null
)