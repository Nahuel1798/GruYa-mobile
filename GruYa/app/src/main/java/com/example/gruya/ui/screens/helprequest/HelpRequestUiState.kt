package com.example.gruya.ui.screens.helprequest

data class HelpRequestUiState (
    val id: Int,
    val driverName: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val estimatedArrival: Int
)

