package com.example.gruya.ui.screens.home_provider

data class HomeProviderUiState(

    val isLoading: Boolean = false,

    val error: String? = null,

    val isOnline: Boolean = false,

    val todayServices: Int = 0,

    val earnings: Double = 0.0,

    val currentLocation: String = "",

    val requests: List<ProviderRequestUi> = emptyList()
)

data class ProviderRequestUi(
    val id: Int,
    val customerName: String,
    val vehicle: String,
    val distance: String,
    val eta: String,
    val address: String
)