package com.example.gruya.ui.screens.request_assistance

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.VehicleRepository
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RequestAssistanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val assistanceRepository: AssistanceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestAssistanceUiState())
    val uiState: StateFlow<RequestAssistanceUiState> = _uiState.asStateFlow()

    private val geocoder = Geocoder(context, Locale.getDefault())

    init {
        loadVehicles()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val vehicles = vehicleRepository.listAll()
            _uiState.update { it.copy(vehicles = vehicles, isLoading = false) }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        _uiState.update { it.copy(selectedVehicleId = vehicleId) }
    }

    fun onIssueTypeSelected(issueType: IssueType) {
        _uiState.update { it.copy(selectedIssueType = issueType) }
    }

    fun onLocationChanged(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(location = Pair(latitude, longitude)) }
        updateAddress(latitude, longitude, isDestination = false)
    }

    fun onDestinationLocationChanged(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(destinationLocation = Pair(latitude, longitude)) }
        updateAddress(latitude, longitude, isDestination = true)
    }

    fun onAddressQueryChanged(query: String) {
        _uiState.update { it.copy(addressQuery = query) }
    }

    fun onDestinationAddressQueryChanged(query: String) {
        _uiState.update { it.copy(destinationAddressQuery = query) }
    }

    fun searchAddress(isDestination: Boolean) {
        val query = if (isDestination) _uiState.value.destinationAddressQuery else _uiState.value.addressQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocationName(query, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        Pair(addr.latitude, addr.longitude)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            _uiState.update { state ->
                if (location != null) {
                    if (isDestination) {
                        state.copy(
                            destinationLocation = location,
                            destinationAddress = query,
                            isLoading = false
                        )
                    } else {
                        state.copy(
                            location = location,
                            address = query,
                            isLoading = false
                        )
                    }
                } else {
                    state.copy(isLoading = false, error = "No se encontró la dirección")
                }
            }
        }
    }

    private fun updateAddress(latitude: Double, longitude: Double, isDestination: Boolean) {
        viewModelScope.launch {
            val addressText = withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        val street = addr.thoroughfare ?: ""
                        val number = addr.subThoroughfare ?: ""
                        val city = addr.locality ?: ""
                        if (street.isNotEmpty()) "$street $number, $city".trim() else addr.getAddressLine(0)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            _uiState.update {
                if (isDestination) {
                    it.copy(destinationAddress = addressText, destinationAddressQuery = addressText ?: "")
                } else {
                    it.copy(address = addressText, addressQuery = addressText ?: "")
                }
            }
        }
    }

    fun onSubmit(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.selectedVehicleId == null) {
            _uiState.update { it.copy(error = "Seleccioná un vehículo") }
            return
        }

        if (state.selectedIssueType == null) {
            _uiState.update { it.copy(error = "Seleccioná un tipo de problema") }
            return
        }

        if (state.location == null) {
            _uiState.update { it.copy(error = "No se pudo obtener la ubicación") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateAssistanceRequest(
                serviceType = ServiceType.AUXILIO,
                vehicleId = state.selectedVehicleId,
                location = Location(
                    latitude = state.location.first,
                    longitude = state.location.second
                ),
                destinationLocation = state.destinationLocation?.let {
                    Location(it.first, it.second)
                },
                issueType = state.selectedIssueType
            )

            val result = assistanceRepository.create(request)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al enviar la solicitud"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
