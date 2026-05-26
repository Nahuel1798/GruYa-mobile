package com.example.gruya.data.remote.dtos.response

data class UserResponse (
    val id : Int,
    val firstName : String,
    val lastName : String,
    val email : String,
    val role : RoleResponse,
    val avatarUrl : String,
    val phone : String
)