package com.example.gruya.data.remote.dtos.response

data class AuthResponse (
    val token : String,
    val user : UserResponse
)