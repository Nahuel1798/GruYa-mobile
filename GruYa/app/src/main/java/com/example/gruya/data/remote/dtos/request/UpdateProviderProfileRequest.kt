package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.ServiceType

data class UpdateProviderProfileRequest(
    val serviceType: ServiceType,
    val companyName: String,
    val address: String,
    val description: String
)
