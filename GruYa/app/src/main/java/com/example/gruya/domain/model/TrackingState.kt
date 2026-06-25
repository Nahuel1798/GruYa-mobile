package com.example.gruya.domain.model

sealed interface TrackingState {
    data object Idle : TrackingState
    data object Connecting : TrackingState
    data class Connected(val sessionId: String) : TrackingState
    data object Tracking : TrackingState
    data object Disconnected : TrackingState
    data class Error(val message: String) : TrackingState
}