package com.example.gruya.ui.screens.requestaux

data class RequestAuxUiState (
    val id: Int,
    val driverName: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val estimatedArrival: Int
)

