package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType

data class ProviderProfileResponse(
    val id: Int,
    val user: UserResponse,
    val description: String,
    val companyName: String,
    val address: String,
    val serviceType: ServiceType,
    val location: Location,
    val currentLocation: Location? = null,
    val lastLocationUpdate: String? = null,
    val isAvailable: Boolean
)