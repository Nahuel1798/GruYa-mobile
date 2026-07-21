package com.example.gruya.ui.screens.request_assistance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.response.FuelStationDto
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.FuelStationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapPickerUiState(
    val nearbyProviders: List<ProviderLocationResponse> = emptyList(),
    val nearbyStations: List<FuelStationDto> = emptyList(),
    val selectedProvider: ProviderLocationResponse? = null,
    val selectedStation: FuelStationDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MapPickerViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val fuelStationRepository: FuelStationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapPickerUiState())
    val uiState: StateFlow<MapPickerUiState> = _uiState.asStateFlow()

    fun loadNearbyProviders(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val providers = assistanceRepository.getProviderlocation(latitude, longitude)
                val filteredProviders = providers.filter { 
                    val type = it.serviceType.uppercase()
                    type == "MECANICO" || type == "GOMERIA" || type == "AUXILIO"
                }

                val stations = try {
                    fuelStationRepository.getNearbyStations(latitude, longitude)
                } catch (e: Exception) {
                    null
                }

                _uiState.update { 
                    it.copy(
                        nearbyProviders = filteredProviders,
                        nearbyStations = stations ?: it.nearbyStations,
                        isLoading = false 
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Error al cargar servicios cercanos",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun selectProvider(provider: ProviderLocationResponse) {
        _uiState.update { it.copy(selectedProvider = provider, selectedStation = null) }
    }

    fun selectStation(station: FuelStationDto) {
        _uiState.update { it.copy(selectedStation = station, selectedProvider = null) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedProvider = null, selectedStation = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
