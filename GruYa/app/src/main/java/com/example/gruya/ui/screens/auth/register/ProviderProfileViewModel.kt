package com.example.gruya.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProviderProfileViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
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

    fun onLocationChange(lat: Double, lng: Double) {
        _uiState.update { it.copy(
            location = Location(0, lat.toString(), lng.toString()),
            latitude = lat,
            longitude = lng
        ) }
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
                location = location
            )
            
            _uiState.update { currentValue ->
                currentValue.copy(success = result, loading = false)
            }
        }
    }
}
