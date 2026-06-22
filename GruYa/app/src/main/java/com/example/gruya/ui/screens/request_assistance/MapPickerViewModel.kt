package com.example.gruya.ui.screens.request_assistance

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
import javax.inject.Inject

data class MapPickerUiState(
    val nearbyProviders: List<ProviderLocationResponse> = emptyList(),
    val selectedProvider: ProviderLocationResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MapPickerViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapPickerUiState())
    val uiState: StateFlow<MapPickerUiState> = _uiState.asStateFlow()

    fun loadNearbyProviders(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val providers = assistanceRepository.getProviderlocation(latitude, longitude)
                val filteredProviders = providers.filter { 
                    it.serviceType.uppercase() == "MECANICO" || it.serviceType.uppercase() == "GOMERIA"
                }
                _uiState.update { 
                    it.copy(
                        nearbyProviders = filteredProviders,
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

    fun selectProvider(provider: ProviderLocationResponse) {
        _uiState.update { it.copy(selectedProvider = provider) }
    }

    fun clearSelectedProvider() {
        _uiState.update { it.copy(selectedProvider = null) }
    }
}
