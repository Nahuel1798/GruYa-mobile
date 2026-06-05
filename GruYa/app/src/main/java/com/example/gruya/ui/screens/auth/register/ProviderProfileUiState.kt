package com.example.gruya.ui.screens.auth.register

import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType

data class ProviderProfileUiState(

    val serviceType: ServiceType? = null,

    val description: String = "",

    val available: Boolean = true,

    val address: String = "",

    val location: Location? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val loading: Boolean = false,

    val success: Boolean = false,

    val error: String? = null
)