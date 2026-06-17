package com.example.gruya.domain.model

data class Quote(
    val id: Int,
    val assistanceId: Int,
    val price: Double,
    val status: QuoteStatus,
    val createdAt: String,
    val updatedAt: String,
    val providerName: String,
    val assistance: Assistance
)
