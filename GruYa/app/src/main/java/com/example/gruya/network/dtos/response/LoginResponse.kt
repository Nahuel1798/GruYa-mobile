package com.example.gruya.network.dtos.response

data class LoginResponse (
    val token : String,
    val user : UserResponse
)