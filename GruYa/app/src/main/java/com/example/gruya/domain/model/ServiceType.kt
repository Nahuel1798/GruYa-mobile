package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class ServiceType(val displayName: String) {
    @SerializedName("Auxilio")
    AUXILIO("Auxilio"),
    @SerializedName("Gomeria")
    GOMERIA("Gomería"),
    @SerializedName("Mecanico")
    MECANICO("Mecánico")
}