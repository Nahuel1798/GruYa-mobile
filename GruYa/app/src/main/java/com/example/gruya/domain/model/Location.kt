package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

data class Location (
    @SerializedName(value = "latitude", alternate = ["Latitude"])
    val latitude: Double,
    @SerializedName(value = "longitude", alternate = ["Longitude"])
    val longitude: Double
)
