package com.example.gruya.data.remote.dtos.response

data class RouteLegResponse (
    val distance: Double,
    val etaMinutes: Double,
    val geometryJson: String
)

data class AssistanceRouteResponse(
    val providerToOrigen: RouteLegResponse,
    val originToDestination: RouteLegResponse
)
