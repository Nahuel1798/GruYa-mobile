package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.ServiceType
import org.maplibre.compose.location.Location

data class CreateHelpRequest (
    val servicetype: ServiceType,
    val vehicleid: Int,
    val createlocationrequest: Location,
    val clienteid: Int,
    val providerid: Int
)