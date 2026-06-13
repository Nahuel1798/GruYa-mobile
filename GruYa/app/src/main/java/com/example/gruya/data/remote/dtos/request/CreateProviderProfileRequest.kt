package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.ServiceType
import com.example.gruya.domain.model.Location

data class CreateProviderProfileRequest (
    val serviceType: ServiceType,
    val companyName: String,
    val description: String,
    val location: Location,
    val address: String
)