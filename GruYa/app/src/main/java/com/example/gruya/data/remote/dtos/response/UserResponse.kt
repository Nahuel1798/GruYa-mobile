package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.Role

data class UserResponse (
    val id : Int,
    val firstName : String,
    val lastName : String,
    val email : String,
    val role: Role?,
    val avatarUrl : String?,
    val phone : String?
)
