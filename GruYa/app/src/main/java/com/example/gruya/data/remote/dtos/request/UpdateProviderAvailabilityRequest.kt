package com.example.gruya.data.remote.dtos.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProviderAvailabilityRequest(
    val isAvailable: Boolean
)
