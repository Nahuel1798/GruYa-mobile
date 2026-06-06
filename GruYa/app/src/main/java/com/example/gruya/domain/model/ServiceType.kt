package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class ServiceType {
    @SerializedName("Auxilio")
    AUXILIO,
    @SerializedName("Gomeria")
    GOMERIA,
    @SerializedName("Mecanico")
    MECANICO
}