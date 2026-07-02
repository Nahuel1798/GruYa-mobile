package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.VehicleType

data class VehicleResponse(
    val id: Int,
    val type: VehicleType,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String,
    val imageUrl: String? = null,
)
