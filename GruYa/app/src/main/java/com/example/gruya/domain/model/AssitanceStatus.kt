package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class AssistanceStatus {
    @SerializedName("Pendiente") PENDIENTE,
    @SerializedName("Aceptada") ACEPTADA,
    @SerializedName("EnCaminoAlCliente") EN_CAMINO_AL_CLIENTE,
    @SerializedName("EnOrigen") EN_ORIGEN,
    @SerializedName("EnCaminoAlDestino") EN_CAMINO_AL_DESTINO,
    @SerializedName("Completado") COMPLETADO,
    @SerializedName("Cancelado") CANCELADO
}

val AssistanceStatus.displayName: String get() = when (this) {
    AssistanceStatus.PENDIENTE -> "Pendiente"
    AssistanceStatus.ACEPTADA -> "Aceptada"
    AssistanceStatus.EN_CAMINO_AL_CLIENTE -> "En camino al cliente"
    AssistanceStatus.EN_ORIGEN -> "En origen"
    AssistanceStatus.EN_CAMINO_AL_DESTINO -> "En camino al destino"
    AssistanceStatus.COMPLETADO -> "Completado"
    AssistanceStatus.CANCELADO -> "Cancelado"
}