package com.example.gruya.domain.model

data class Vehicle(
    val id: Int,
    val type: VehicleType,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String,
    val imageUrl: String? = null
)