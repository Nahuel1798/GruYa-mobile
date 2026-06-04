package com.example.gruya.ui.screens.home

import com.google.android.gms.maps.model.LatLng

data class HomeUiState (
    val isLoading : Boolean = false,

    val hasLocationPermission: Boolean = false,

    val userLocation: LatLng? = null,

    val searchText: String = "",

    val showDialog: Boolean = false,

    val panelVisible: Boolean = true,

    val nearbyTowTrucks: List<LatLng> = listOf(
        LatLng(-33.3000, -66.3400),
        LatLng(-33.2900, -66.3300),
        LatLng(-33.3050, -66.3250)
    ),

    val isMapFullScreen: Boolean = false,

    val error: String? = null
)