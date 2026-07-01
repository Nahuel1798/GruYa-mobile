package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class QuoteStatus {
    @SerializedName("Pendiente")
    PENDIENTE,
    @SerializedName("Aceptada")
    ACEPTADA,
    @SerializedName("Completado")
    COMPLETADO,
    @SerializedName("Rechazada")
    RECHAZADA,
    @SerializedName("Cancelada")
    CANCELADA,
    @SerializedName("Expirada")
    EXPIRADA
}
