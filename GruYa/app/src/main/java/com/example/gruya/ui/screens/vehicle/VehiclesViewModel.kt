package com.example.gruya.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.VehicleRepository
import com.example.gruya.domain.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VehiclesNavigationEvent {
    data object AddVehicle : VehiclesNavigationEvent
    data class EditVehicle(val vehicleId: Int) : VehiclesNavigationEvent
}

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiclesUiState())
    val uiState: StateFlow<VehiclesUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<VehiclesNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun listVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val vehicles = vehicleRepository.listAll()
            _uiState.update { it.copy(vehicles = vehicles, isLoading = false) }
        }
    }

    fun onAddVehicle() {
        viewModelScope.launch {
            _navigationEvent.send(VehiclesNavigationEvent.AddVehicle)
        }
    }

    fun onEditClick(vehicle: Vehicle) {
        viewModelScope.launch {
            _navigationEvent.send(VehiclesNavigationEvent.EditVehicle(vehicle.id))
        }
    }

    fun onDeleteClick(vehicle: Vehicle) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                vehicleSelect = vehicle
            )
        }
    }

    fun confirmDelete() {
        val vehicle = _uiState.value.vehicleSelect ?: return
        viewModelScope.launch {
            val success = vehicleRepository.delete(vehicle.id)
            if (success) {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        vehicleSelect = null,
                        vehicles = it.vehicles.filter { v -> v.id != vehicle.id }
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        vehicleSelect = null,
                        error = "Error al eliminar el vehículo"
                    )
                }
            }
        }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                vehicleSelect = null
            )
        }
    }

}
