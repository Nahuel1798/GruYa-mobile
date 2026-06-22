package com.example.gruya.ui.screens.provider_quotes

import kotlinx.serialization.Serializable

@Serializable
enum class ProviderQuoteFilter(
    val label: String,
    val description: String
) {
    ACEPTADAS(
        label = "Aceptadas",
        description = "Cotizaciones aceptadas — servicio en curso"
    ),
    PENDIENTES(
        label = "Pendientes",
        description = "Esperando respuesta del cliente"
    ),
    FINALIZADAS(
        label = "Finalizadas",
        description = "Cotizaciones rechazadas, canceladas o expiradas"
    )
}
