package com.example.gruya.ui.screens.assistance_tracking

import com.example.gruya.data.remote.dtos.response.AssistanceResponse

data class AssistanceTrackingUiState(
    val assistance: AssistanceResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val originAddress: String? = null,
    val destinationAddress: String? = null
)
