package com.example.gruya.ui.screens.home_provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.spatialk.geojson.Position

@HiltViewModel
class HomeProviderViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeProviderUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNearbyAssistances()
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(userLocation = Position(longitude, latitude))
        }
    }

    fun updateLocationName(name: String) {
        _uiState.update { it.copy(currentLocation = name) }
    }

    fun loadNearbyAssistances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val assistances = assistanceRepository.getNearbyAssistances()
                _uiState.update {
                    it.copy(
                        nearbyAssistances = assistances,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleAvailability() {
        _uiState.update {
            it.copy(isOnline = !it.isOnline)
        }
    }
}
