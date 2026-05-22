package com.example.gruya.network.dtos.response

import com.example.gruya.models.Role

data class UserResponse (
    val id : Int,
    val firstName : String,
    val lastName : String,
    val email : String,
    val role : RoleResponse,
    val avatarUrl : String,
    val phone : String
)