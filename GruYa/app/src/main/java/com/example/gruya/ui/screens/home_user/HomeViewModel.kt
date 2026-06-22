package com.example.gruya.ui.screens.home_user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.FuelStationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.spatialk.geojson.Position
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val fuelStationRepository: FuelStationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    fun updateUserLocation(latitud: Double, longitud: Double) {
        _uiState.update {
            it.copy(userLocation = Position(longitud, latitud))
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(searchText = value) }
    }

    fun toggleMapFullScreen() {
        _uiState.update {
            it.copy(
                isMapFullScreen = !it.isMapFullScreen,
                // FIXED: mostrar panel al SALIR del fullscreen, no al entrar
                panelVisible = it.isMapFullScreen
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, stationsError = null) }
    }

    fun selectProvider(provider: ProviderLocationResponse) {
        _uiState.update { it.copy(selectedProvider = provider) }
    }

    fun clearSelectedProvider() {
        _uiState.update { it.copy(selectedProvider = null) }
    }

    fun loadService(lat: Double? = null, lon: Double? = null) {
        val latitude = lat ?: _uiState.value.userLocation?.latitude ?: return
        val longitude = lon ?: _uiState.value.userLocation?.longitude ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch providers
                val providers = assistanceRepository.getProviderlocation(
                    latitude = latitude,
                    longitude = longitude
                )
                
                // Fetch fuel stations
                val stations = try {
                    val result = fuelStationRepository.getNearbyStations(latitude, longitude)
                    _uiState.update { it.copy(stationsError = null) }
                    result
                } catch (e: Exception) {
                    Log.e("GRUYA", "Error cargando estaciones", e)
                    val errorMsg = if (e.message?.contains("504") == true || e.message?.contains("timeout") == true) {
                        "El servidor de estaciones (Overpass) está saturado. Reintente en unos momentos."
                    } else {
                        "No se pudieron cargar las estaciones de servicio."
                    }
                    _uiState.update { it.copy(stationsError = errorMsg) }
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        nearbyTowTrucks = providers,
                        nearbyFuelStations = stations,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("GRUYA", "Error cargando proveedores", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar servicios cercanos"
                    )
                }
            }
        }
    }
}
