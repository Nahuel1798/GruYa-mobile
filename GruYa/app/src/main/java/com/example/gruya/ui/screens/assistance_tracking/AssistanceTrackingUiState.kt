package com.example.gruya.ui.screens.assistance_tracking

import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.Payment
import com.example.gruya.domain.model.Quote
import com.example.gruya.domain.model.TrackingState
import org.maplibre.spatialk.geojson.Position

data class AssistanceTrackingUiState(
    val assistance: AssistanceResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val trackingState: TrackingState = TrackingState.Idle,
    val providerLocation: Location? = null,
    val isProvider: Boolean = false,
    val providerToOriginRoute: String? = null,
    val providerToDestinationRoute: String? = null,
    val providerToOriginPositions: List<Position> = emptyList(),
    val providerToDestinationPositions: List<Position> = emptyList(),
    val assistanceRoutePositions: List<Position> = emptyList(),
    val isNearOrigin: Boolean = false,
    val isNearDestination: Boolean = false,
    val payment: Payment? = null,
    val acceptedQuote: Quote? = null
)
