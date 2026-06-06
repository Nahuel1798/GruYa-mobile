package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.VehicleType

data class UpdateVehicleRequest(
    val type: VehicleType,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String
)
