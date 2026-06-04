package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.VehicleType

data class CreateVehicleRequest (
    val vehicletype: VehicleType,
    val licenseplate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String
)