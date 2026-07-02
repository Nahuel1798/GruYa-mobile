package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.VehicleType

data class CreateVehicleRequest (
    val type: VehicleType,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String,
    val imageUrl: String? = null
)