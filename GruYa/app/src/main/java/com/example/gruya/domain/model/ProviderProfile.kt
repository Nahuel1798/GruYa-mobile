package com.example.gruya.domain.model

data class ProviderProfile(
    val id: Int,
    val description: String,
    val companyName: String,
    val address: String,
    val serviceType: ServiceType,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean
)
