package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType

data class CreateAssistanceRequest(
    val serviceType: ServiceType,
    val vehicleId: Int,
    val location: Location,
    val issueType: IssueType,
    val providerId: Int? = null
)
