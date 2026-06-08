package com.example.gruya.ui.screens.home_provider

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeProviderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeProviderUiState(
            requests = listOf(
                ProviderRequestUi(
                    id = 1,
                    customerName = "Marcos Rodriguez",
                    vehicle = "Toyota Corolla",
                    distance = "0.8 km",
                    eta = "4 min",
                    address = "Av. Santa Fe 2450"
                ),
                ProviderRequestUi(
                    id = 2,
                    customerName = "Lucía Méndez",
                    vehicle = "Ford EcoSport",
                    distance = "2.4 km",
                    eta = "12 min",
                    address = "Callao 1500"
                )
            )
        )
    )

    val uiState: StateFlow<HomeProviderUiState> =
        _uiState.asStateFlow()

    fun toggleAvailability() {

        _uiState.value = _uiState.value.copy(
            isOnline = !_uiState.value.isOnline
        )
    }

    fun acceptRequest(requestId: Int) {

        _uiState.value = _uiState.value.copy(
            requests = _uiState.value.requests.filter {
                it.id != requestId
            }
        )
    }
}