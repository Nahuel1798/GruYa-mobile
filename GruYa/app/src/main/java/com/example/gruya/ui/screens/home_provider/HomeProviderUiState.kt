package com.example.gruya.ui.screens.home_provider

data class HomeProviderUiState (
    val isOnline: Boolean = false,

    val todayServices: Int = 12,

    val earnings: Double = 45.20,

    val currentLocation: String = "San Luis, Argentina",

    val requests: List<ProviderRequestUi> = emptyList(),

    val loading: Boolean = false
)

data class ProviderRequestUi(
    val id: Int,
    val customerName: String,
    val vehicle: String,
    val distance: String,
    val eta: String,
    val address: String
)