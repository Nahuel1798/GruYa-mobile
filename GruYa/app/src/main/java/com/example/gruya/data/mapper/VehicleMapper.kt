package com.example.gruya.data.mapper

import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import com.example.gruya.domain.model.Vehicle

fun VehicleResponse.toDomain(): Vehicle = Vehicle(
    id = id,
    type = type,
    licensePlate = licensePlate,
    brand = brand,
    model = model,
    insurance = insurance,
    color = color
)

fun List<VehicleResponse>.toDomain(): List<Vehicle> = map { it.toDomain() }

fun Vehicle.toRequest(): CreateVehicleRequest = CreateVehicleRequest(
    type = type,
    licensePlate = licensePlate,
    brand = brand,
    model = model,
    insurance = insurance,
    color = color
)
