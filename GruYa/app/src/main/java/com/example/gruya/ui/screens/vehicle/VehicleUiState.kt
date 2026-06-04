package com.example.gruya.ui.screens.vehicle

import android.app.Dialog
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.domain.model.VehicleType

data class VehicleUiState (
    val isLoading: Boolean = false,

    //Formulario
    val vehicletype: VehicleType = VehicleType.AUTO,
    val licensePlate: String = "",
    val brand: String = "",
    val model: String = "",
    val insurance: String = "",
    val color: String = "",

    //Lista
    val vehicle: List<CreateVehicleRequest> = emptyList(),

    // Estados UI
    val showDeleteDialog: Boolean = false,
    val vehicleSelec: CreateVehicleRequest? = null,
    val error: String? = null
)