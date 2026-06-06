package com.example.gruya.ui.screens.vehicle

import com.example.gruya.domain.model.Vehicle

data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val vehicleSelect: Vehicle? = null
)