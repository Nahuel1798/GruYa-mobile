package com.example.gruya.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VehicleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        VehicleUiState(
            vehicle = listOf(

            )
        )
    )

    val uiState : StateFlow<VehicleUiState> = _uiState.asStateFlow()

    fun onAgregarVehiculo() {
        // agregar metodo
    }

    fun onEditarVehiculo() {

    }

    fun onEliminarClick() {

    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                vehicleSelec = null
            )
        }
    }

}