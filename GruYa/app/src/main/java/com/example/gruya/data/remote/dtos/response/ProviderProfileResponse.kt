package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.ServiceType
import com.example.gruya.domain.model.User

data class ProviderProfileResponse(
    val id: Int,
    val user: User,
    val descripcion: String,
    val serviceType: ServiceType,
    val locationResponse: LocationResponse,
    val isAvailable: Boolean
)