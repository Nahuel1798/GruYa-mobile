package com.example.gruya.ui.screens.assistance_tracking

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssistanceTrackingViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistanceTrackingUiState())
    val uiState: StateFlow<AssistanceTrackingUiState> = _uiState.asStateFlow()

    private val geocoder = Geocoder(context, Locale.getDefault())

    fun loadAssistance(assistanceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.getAssistanceDetails(assistanceId)
            result.fold(
                onSuccess = { assistance ->
                    _uiState.update { it.copy(assistance = assistance, isLoading = false) }
                    if (assistance != null) {
                        updateAddresses(assistance)
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    private fun updateAddresses(assistance: AssistanceResponse) {
        viewModelScope.launch {
            val origin = assistance.origin
            val destination = assistance.destination

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(origin.latitude, origin.longitude, 1) { addresses ->
                    _uiState.update { it.copy(originAddress = addresses.firstOrNull()?.getAddressLine(0)) }
                }
                geocoder.getFromLocation(destination.latitude, destination.longitude, 1) { addresses ->
                    _uiState.update { it.copy(destinationAddress = addresses.firstOrNull()?.getAddressLine(0)) }
                }
            } else {
                @Suppress("DEPRECATION")
                val originAddr = geocoder.getFromLocation(origin.latitude, origin.longitude, 1)?.firstOrNull()?.getAddressLine(0)
                @Suppress("DEPRECATION")
                val destAddr = geocoder.getFromLocation(destination.latitude, destination.longitude, 1)?.firstOrNull()?.getAddressLine(0)
                _uiState.update { it.copy(originAddress = originAddr, destinationAddress = destAddr) }
            }
        }
    }
}
