package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType

data class AssistanceResponse(
    val id: Int,
    val serviceType: ServiceType,
    val issueType: IssueType,
    val status: AssistanceStatus,
    val vehicle: VehicleResponse,
    val providerProfile: ProviderProfileResponse?,
    val client: UserResponse,
    val distanceKm: Double?,
    val etaMinutes: Double?,
    val routeGeometry: String?,
    val origin: Location,
    val destination: Location,
    val isDirected: Boolean
)
