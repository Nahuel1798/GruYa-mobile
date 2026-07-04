package com.example.gruya.ui.screens.home_user

import com.example.gruya.data.remote.dtos.response.FuelStationDto
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import org.maplibre.spatialk.geojson.Position

data class HomeUiState(

    val isLoading: Boolean = false,

    val hasLocationPermission: Boolean = false,

    val userLocation: Position? = null,

    val searchText: String = "",

    val panelVisible: Boolean = true,

    val nearbyTowTrucks: List<ProviderLocationResponse> = emptyList(),

    val nearbyFuelStations: List<FuelStationDto> = emptyList(),

    val selectedProvider: ProviderLocationResponse? = null,

    val isMapFullScreen: Boolean = false,

    val error: String? = null,

    val stationsError: String? = null,

    val unreadNotificationsCount: Int = 0
)
