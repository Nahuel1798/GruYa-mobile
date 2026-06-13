package com.example.gruya.ui.screens.home_user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.repository.AssistanceRepository
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
    private val assistanceRepository: AssistanceRepository
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
        _uiState.update { it.copy(error = null) }
    }

    // FIXED: requestTowTruck ahora limpia isLoading y cierra el sheet al terminar
    fun requestTowTruck() {
        val location = _uiState.value.userLocation ?: run {
            _uiState.update { it.copy(error = "No se pudo obtener tu ubicación") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // TODO: reemplazar con llamada real a assistanceRepository.create(...)
                // assistanceRepository.create(request)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedProvider = null   // cierra el ModalBottomSheet
                    )
                }
            } catch (e: Exception) {
                Log.e("GRUYA", "Error solicitando grúa", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al solicitar el servicio"
                    )
                }
            }
        }
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
                val providers = assistanceRepository.getProviderlocation(
                    latitude = latitude,
                    longitude = longitude
                )
                Log.d("GRUYA", "Providers encontrados: ${providers.size}")
                providers.forEach {
                    Log.d("GRUYA", "  - ${it.companyName} (${it.latitude}, ${it.longitude})")
                }
                _uiState.update {
                    it.copy(nearbyTowTrucks = providers, isLoading = false)
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
