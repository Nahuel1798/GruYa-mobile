package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class AssistanceStatus {
    @SerializedName("Pendiente")
    PENDIENTE,
    @SerializedName("EnProceso")
    EN_PROCESO,
    @SerializedName("Completado")
    COMPLETADO,
    @SerializedName("Cancelado")
    CANCELADO
}