package com.example.gruya.ui.screens.auth.register

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ProviderProfileViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun onCompanyNameChange(value: String) {
        _uiState.update { it.copy(companyName = value) }
    }

    fun onServiceTypeChange(value: ServiceType) {
        _uiState.update { it.copy(serviceType = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onAvailableChange(value: Boolean) {
        _uiState.update { it.copy(available = value) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun searchAddress() {
        val address = _uiState.value.address
        if (address.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true) }
                val geocoder = Geocoder(context)
                val addresses = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(address, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    onLocationChange(location.latitude, location.longitude)
                } else {
                    _uiState.update { it.copy(error = "No se pudo encontrar la ubicación") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al buscar la ubicación: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun onLocationChange(lat: Double, lng: Double) {
        _uiState.update { it.copy(
            location = Location(lat, lng),
            latitude = lat,
            longitude = lng
        ) }

        // Reverse geocoding to get address name
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context)
                val addresses = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressLine = address.getAddressLine(0)
                    _uiState.update { it.copy(address = addressLine) }
                }
            } catch (e: Exception) {
                // Ignore error in reverse geocoding, just keep coordinates
            }
        }
    }

    fun createProfile(){
        val currentState = _uiState.value
        val serviceType = currentState.serviceType ?: return
        val location = currentState.location ?: return
        
        viewModelScope.launch { 
            _uiState.update { it.copy(loading = true) }
            val result = providerRepository.create(
                serviceType = serviceType,
                companyName = currentState.companyName,
                description = currentState.description,
                location = location,
                address = currentState.address,
                isAvailable = currentState.available
            )
            
            _uiState.update { currentValue ->
                currentValue.copy(success = result, loading = false)
            }
        }
    }
}
