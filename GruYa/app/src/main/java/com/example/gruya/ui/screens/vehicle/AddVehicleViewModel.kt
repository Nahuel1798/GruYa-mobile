package com.example.gruya.ui.screens.vehicle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.request.UpdateVehicleRequest
import com.example.gruya.data.repository.VehicleRepository
import com.example.gruya.domain.model.VehicleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddVehicleViewModel(application: Application) : AndroidViewModel(application) {

    private val vehicleRepository = VehicleRepository()
    private val sessionManager = SessionManager(getApplication())

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    fun loadVehicle(vehicleId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val vehicle = vehicleRepository.getById(sessionManager.getJwt(), vehicleId)
            if (vehicle != null) {
                _uiState.update {
                    it.copy(
                        isEditMode = true,
                        vehicleId = vehicle.id,
                        selectedType = vehicle.type,
                        plate = vehicle.licensePlate,
                        brand = vehicle.brand,
                        model = vehicle.model,
                        insurer = vehicle.insurance,
                        color = vehicle.color,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudo cargar el vehículo"
                    )
                }
            }
        }
    }

    fun onTypeSelected(type: VehicleType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onPlateChange(plate: String) {
        _uiState.update {
            it.copy(
                plate = plate,
                plateError = if (plate.isNotBlank() || plate.isEmpty()) null else "La patente es requerida"
            )
        }
    }

    fun onBrandChange(brand: String) {
        _uiState.update {
            it.copy(
                brand = brand,
                brandError = if (brand.isNotBlank() || brand.isEmpty()) null else "La marca es requerida"
            )
        }
    }

    fun onModelChange(model: String) {
        _uiState.update {
            it.copy(
                model = model,
                modelError = if (model.isNotBlank() || model.isEmpty()) null else "El modelo es requerido"
            )
        }
    }

    fun onInsurerChange(insurer: String) {
        _uiState.update { it.copy(insurer = insurer) }
    }

    fun onColorChange(color: String) {
        _uiState.update {
            it.copy(
                color = color,
                colorError = if (color.isNotBlank() || color.isEmpty()) null else "El color es requerido"
            )
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            val request = CreateVehicleRequest(
                type = state.selectedType,
                licensePlate = state.plate,
                brand = state.brand,
                model = state.model,
                insurance = state.insurer,
                color = state.color
            )

            val success = if (state.isEditMode && state.vehicleId != null) {
                val updateRequest = UpdateVehicleRequest(
                    type = state.selectedType,
                    licensePlate = state.plate,
                    brand = state.brand,
                    model = state.model,
                    insurance = state.insurer,
                    color = state.color
                )
                vehicleRepository.update(sessionManager.getJwt(), state.vehicleId, updateRequest) != null
            } else {
                vehicleRepository.create(sessionManager.getJwt(), request) != null
            }

            if (success) {
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al guardar el vehículo. Intente nuevamente."
                    )
                }
            }
        }
    }

    private fun validate(): Boolean {
        var valid = true
        _uiState.update { state ->
            var s = state
            if (state.plate.isBlank()) {
                s = s.copy(plateError = "La patente es requerida")
                valid = false
            }
            if (state.brand.isBlank()) {
                s = s.copy(brandError = "La marca es requerida")
                valid = false
            }
            if (state.model.isBlank()) {
                s = s.copy(modelError = "El modelo es requerido")
                valid = false
            }
            if (state.color.isBlank()) {
                s = s.copy(colorError = "El color es requerido")
                valid = false
            }
            s
        }
        return valid
    }
}
