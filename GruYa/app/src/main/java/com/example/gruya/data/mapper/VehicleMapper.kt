package com.example.gruya.data.mapper

import android.util.Log
import com.example.gruya.data.remote.Constants
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
    color = color,
    imageUrl = if (imageUrl.isNullOrBlank()) null else {
        val it = imageUrl
        val fullUrl = if (it.startsWith("http")) {
            // Forzar el uso del mismo host/puerto que la API para evitar problemas de localhost/IPs locales
            val baseHostPort = Constants.BASE_URL.substringAfter("://").removeSuffix("/")
            val path = it.substringAfter("://").substringAfter("/", "")
            if (path.isEmpty()) it else "${Constants.BASE_URL.substringBefore("://")}://$baseHostPort/$path"
        } else {
            val cleanPath = it.replace("\\", "/").removePrefix("/")
            "${Constants.BASE_URL.removeSuffix("/")}/$cleanPath"
        }
        Log.d("VehicleMapper", "Mapped imageUrl for vehicle $id: $fullUrl")
        fullUrl
    }
)

fun List<VehicleResponse>.toDomain(): List<Vehicle> = map { it.toDomain() }

fun Vehicle.toRequest(): CreateVehicleRequest = CreateVehicleRequest(
    type = type,
    licensePlate = licensePlate,
    brand = brand,
    model = model,
    insurance = insurance,
    color = color,
    imageUrl = imageUrl
)
