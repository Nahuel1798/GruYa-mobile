package com.example.gruya.ui.screens.home_user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.maplibre.spatialk.geojson.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

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

    fun loadService(){
        val location = _uiState.value.userLocation ?: return

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true)
                }

                val providers =
                    serviceRepository.getProviderlocation(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                Log.d("GRUYA", "Providers encontrados: ${providers.size}")
                providers.forEach {
                    Log.d(
                        "GRUYA",
                        "Lat: ${it.latitude} Lon: ${it.longitude}"
                    )
                }
                val position = providers.map {
                    Position(
                        it.longitude,
                        it.latitude
                    )
                }

                _uiState.update {
                    it.copy(
                        nearbyTowTrucks = providers,
                        isLoading = false
                    )
                }

            }catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false)
                }

                e.printStackTrace()
            }
        }
    }
}