package com.example.gruya.data.remote.dtos.request

data class UpdatePasswordRequest (
    val old: String,
    val new: String
)