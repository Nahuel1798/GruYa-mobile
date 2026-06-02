package com.example.gruya.data.remote.dtos.request

data class UpdateUserRequest (
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)