package com.example.gruya.ui.screens.home_user

import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import org.maplibre.spatialk.geojson.Position

data class HomeUiState(
    val isLoading: Boolean = false,

    val hasLocationPermission: Boolean = false,

    val userLocation: Position? = null,

    val searchText: String = "",

    val showDialog: Boolean = false,

    val panelVisible: Boolean = true,

    val nearbyTowTrucks: List<ProviderLocationResponse> = emptyList(),

    val isMapFullScreen: Boolean = false,

    val error: String? = null
)