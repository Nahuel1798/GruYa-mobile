package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.QuoteStatus

data class QuoteResponse(
    val id: Int,
    val assistanceId: Int,
    val price: Double,
    val status: QuoteStatus,
    val createdAt: String,
    val updatedAt: String,
    val providerName: String,
    val assistance: AssistanceResponse?
)
