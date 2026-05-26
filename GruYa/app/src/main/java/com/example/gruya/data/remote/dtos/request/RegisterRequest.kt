package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.Role

data class RegisterRequest(
    val firstName : String,
    val lastName : String,
    val email : String,
    val password : String,
    val phone : String,
    val role : Role
)