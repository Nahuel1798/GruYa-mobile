package com.example.gruya.data.remote.dtos.response

data class RouteLegResponse (
    val distanceKm: Double?,
    val etaMinutes: Double?,
    val geometryJson: String?
)

data class AssistanceRouteResponse(
    val providerToOrigin: RouteLegResponse?,
    val originToDestination: RouteLegResponse?,
    val providerToDestination: RouteLegResponse?
)
