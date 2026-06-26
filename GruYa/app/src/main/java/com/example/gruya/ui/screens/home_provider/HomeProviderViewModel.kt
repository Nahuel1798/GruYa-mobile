package com.example.gruya.ui.screens.home_provider

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.request.UpdateProviderProfileRequest
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
                        _uiState.update {
                            it.copy(
                                isProfileComplete = true,
                                providerProfile = profile,
                                isOnline = false,
                                profileCheckError = null
                            )
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
        updateBackendAvailability(true)
    }

    private fun goOffline() {
        val intent = Intent(application, ProviderLocationService::class.java)
        application.stopService(intent)
        updateBackendAvailability(false)
    }

    private fun updateBackendAvailability(available: Boolean) {
        val profile = _uiState.value.providerProfile ?: return
        viewModelScope.launch {
            providerRepository.updateProfile(
                UpdateProviderProfileRequest(
                    serviceType = profile.serviceType,
                    companyName = profile.companyName,
                    address = profile.address,
                    description = profile.description,
                    isAvailable = available
                )
            )
        }
    }
}
