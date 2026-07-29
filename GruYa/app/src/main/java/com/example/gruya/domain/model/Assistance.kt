package com.example.gruya.domain.model

data class Assistance(
    val id: Int,
    val serviceType: ServiceType,
    val issueType: IssueType,
    val status: AssistanceStatus,
    val vehicle: Vehicle,
    val origin: Location,
    val destination: Location,
    val isDirected: Boolean,
    val createdAt: String? = null,
    val clientName: String = "",
    val routeGeometry: String? = null,
    val payment: Payment? = null,
    val trackingSessionId: String? = null,
    val distanceKm: Double? = null,
    val etaMinutes: Double? = null
)
