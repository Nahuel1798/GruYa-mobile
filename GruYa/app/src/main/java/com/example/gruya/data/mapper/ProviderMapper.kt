package com.example.gruya.data.mapper

import com.example.gruya.data.remote.dtos.request.UpdateProviderProfileRequest
import com.example.gruya.data.remote.dtos.response.ProviderProfileResponse
import com.example.gruya.domain.model.ProviderProfile

fun ProviderProfileResponse.toDomain(): ProviderProfile = ProviderProfile(
    id = id,
    description = description,
    companyName = companyName,
    address = address,
    serviceType = serviceType,
    latitude = location.latitude,
    longitude = location.longitude,
    isAvailable = isAvailable
)

fun ProviderProfile.toUpdateRequest(): UpdateProviderProfileRequest =
    UpdateProviderProfileRequest(
        serviceType = serviceType,
        companyName = companyName,
        address = address,
        description = description
    )
