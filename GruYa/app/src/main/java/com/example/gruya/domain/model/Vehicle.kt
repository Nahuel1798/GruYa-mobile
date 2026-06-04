package com.example.gruya.domain.model

data class Vehicle(
    val id: Int,
    val vehicletype: VehicleType,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String
)