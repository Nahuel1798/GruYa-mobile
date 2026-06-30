package com.example.gruya.ui.screens.home_provider

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.data.service.ProviderLocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.spatialk.geojson.Position

@HiltViewModel
class HomeProviderViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val providerRepository: ProviderRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeProviderUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkProfileCompletion()
        loadNearbyAssistances()
    }

    fun checkProfileCompletion() {
        viewModelScope.launch {
            providerRepository.getMyProfile()
                .onSuccess { profile ->
                    if (profile != null) {
                        val isFirstCheck = _uiState.value.isProfileComplete == null
                        
                        _uiState.update {
                            it.copy(
                                isProfileComplete = true,
                                providerProfile = profile,
                                isOnline = profile.isAvailable,
                                profileCheckError = null
                            )
                        }

                        // Si el perfil ya está disponible en el backend al iniciar,
                        // nos aseguramos de que el servicio de ubicación esté corriendo.
                        if (isFirstCheck && profile.isAvailable) {
                            val intent = Intent(application, ProviderLocationService::class.java)
                            application.startForegroundService(intent)
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isProfileComplete = false,
                                profileCheckError = null
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isProfileComplete = null,
                            profileCheckError = "No pudimos verificar tu perfil. Intentá de nuevo en un rato."
                        )
                    }
                }
        }
    }

    fun retryProfileCheck() {
        _uiState.update { it.copy(profileCheckError = null) }
        checkProfileCompletion()
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (!granted && _uiState.value.isOnline) {
            goOffline()
            _uiState.update { it.copy(isOnline = false) }
        }
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
        val goingOnline = !_uiState.value.isOnline
        _uiState.update { it.copy(isOnline = goingOnline) }

        if (goingOnline) {
            goOnline()
        } else {
            goOffline()
        }
    }

    private fun goOnline() {
        val intent = Intent(application, ProviderLocationService::class.java)
        application.startForegroundService(intent)
        syncAvailabilityStatus(true)
    }

    private fun goOffline() {
        val intent = Intent(application, ProviderLocationService::class.java)
        application.stopService(intent)
        syncAvailabilityStatus(false)
    }

    private fun syncAvailabilityStatus(available: Boolean) {
        viewModelScope.launch {
            providerRepository.updateAvailability(available)
                .onSuccess {
                    Log.d("HomeProviderViewModel", "Disponibilidad sincronizada: $available")
                }
                .onFailure { e ->
                    Log.e("HomeProviderViewModel", "Error al sincronizar disponibilidad: ${e.message}", e)
                    // Revertimos el estado en la UI si falla la sincronización
                    _uiState.update { 
                        it.copy(
                            isOnline = !available,
                            error = "Error al sincronizar disponibilidad: ${e.message}"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
