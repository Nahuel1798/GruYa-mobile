package com.example.gruya.ui.screens.quotes_list

import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.Quote
import com.example.gruya.domain.model.TrackingState
import org.maplibre.spatialk.geojson.Position

data class QuotesListUiState(
    val quotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionLoading: Int? = null,
    val trackingState: TrackingState = TrackingState.Idle,
    val providerLocation: Location? = null,
    val providerToOriginRoute: String? = null,
    val providerToDestinationRoute: String? = null,
    val providerToOriginPositions: List<Position> = emptyList(),
    val providerToDestinationPositions: List<Position> = emptyList(),
    val assistanceRoutePositions: List<Position> = emptyList(),
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val distanceKm: Double? = null,
    val etaMinutes: Double? = null
)
