package com.example.gruya.data.remote.dtos.response

data class ProviderLocationResponse (
    val id: Int,
    val userId: Int,
    val description: String,
    val serviceType: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean
)