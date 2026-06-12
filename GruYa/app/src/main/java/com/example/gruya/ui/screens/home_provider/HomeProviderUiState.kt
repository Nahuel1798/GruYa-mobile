package com.example.gruya.ui.screens.home_provider

import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import org.maplibre.spatialk.geojson.Position

data class HomeProviderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = false,
    val todayServices: Int = 0,
    val earnings: Double = 0.0,
    val currentLocation: String = "",
    val nearbyAssistances: List<NearbyAssistanceResponse> = emptyList(),
    val hasLocationPermission: Boolean = false,
    val userLocation: Position? = null
)
