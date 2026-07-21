package com.example.gruya.data.remote.dtos.response

//data class RouteInstruction(
//    val text: String,
//    val distanceMeters: Double,
//    val durationSeconds: Double
//)
data class RouteLegResponse (
    val distanceKm: Double?,
    val etaMinutes: Double?,
    val geometryJson: String?
//    val instructions: List<RouteInstruction>
)

data class AssistanceRouteResponse(
    val providerToOrigin: RouteLegResponse?,
    val originToDestination: RouteLegResponse?,
    val providerToDestination: RouteLegResponse?
)
