package com.example.gruya.data.remote.dtos.response

data class NearbyAssistanceResponse(
    val id: Int,
    val serviceType: String,
    val issueType: String?,
    val clientName: String,
    val vehicle: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double
)