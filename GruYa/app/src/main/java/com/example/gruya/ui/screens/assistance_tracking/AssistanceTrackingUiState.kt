package com.example.gruya.ui.screens.assistance_tracking

import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.TrackingState

data class AssistanceTrackingUiState(
    val assistance: AssistanceResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val trackingState: TrackingState = TrackingState.Idle,
    val providerLocation: Location? = null,
    val isProvider: Boolean = false,
    val providerToOriginRoute: String? = null
)
