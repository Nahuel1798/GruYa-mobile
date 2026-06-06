package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class VehicleType(val label: String) {
    @SerializedName("Auto")
    AUTO("Auto"),
    @SerializedName("Camioneta")
    CAMIONETA("Camioneta"),
    @SerializedName("Moto")
    MOTO("Moto")
}