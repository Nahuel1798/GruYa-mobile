package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.Location

data class NearbyAssistanceResponse(
    val id: Int,
    val serviceType: String,
    val issueType: String?,
    val clientName: String,
    val vehicle: String,
    val origin: Location,
    val destination: Location,
    val distanceKm: Double,
    val isDirected: Boolean
)