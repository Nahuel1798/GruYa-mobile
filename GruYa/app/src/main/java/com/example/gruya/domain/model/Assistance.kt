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
    val clientName: String = ""
)
