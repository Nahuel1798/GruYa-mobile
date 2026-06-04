package com.example.gruya.ui.screens.home_user

import androidx.lifecycle.ViewModel
import org.maplibre.spatialk.geojson.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState()
    )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    fun onLocationPermissionChanged(
        granted : Boolean
    ) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted
            )
        }
    }

    fun updateUserLocation(
        latitud: Double,
        longitud: Double
    ) {
        _uiState.update {
            it.copy(
                userLocation = Position(
                    longitud,
                    latitud
                )
            )
        }
    }

    fun onSearchChange(
        value: String
    ) {
        _uiState.update {
            it.copy(
                searchText = value
            )
        }
    }

    fun showRequestDialog() {
        _uiState.update {
            it.copy(
                showDialog = true
            )
        }
    }

    fun hideRequestDialog() {
        _uiState.update {
            it.copy(
                showDialog = false
            )
        }
    }

    fun showPanel() {
        _uiState.update {
            it.copy(
                panelVisible = true
            )
        }
    }

    fun hidePanel() {
        _uiState.update {
            it.copy(
                panelVisible = false
            )
        }
    }

    fun toggleMapFullScreen() {
        _uiState.update {
            it.copy(
                isMapFullScreen = !it.isMapFullScreen,
                panelVisible = it.isMapFullScreen // Mostrar el panel si salimos de pantalla completa
            )
        }
    }

    fun requestTowTruck(){
        _uiState.update {
            it.copy(
                isLoading = true
            )
        }
        // LLamar a la api para obtener las gruas cercanas
    }

    fun loadTowTruks(
        truks: List<Position>
    ) {
        _uiState.update {
            it.copy(
                nearbyTowTrucks = truks
            )
        }
    }
}