package com.example.gruya.data.mapper

import com.example.gruya.data.remote.dtos.response.QuoteResponse
import com.example.gruya.domain.model.Quote

fun QuoteResponse.toDomain(): Quote = Quote(
    id = id,
    assistanceId = assistanceId,
    price = price,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    providerName = providerName,
    assistance = assistance!!.toDomain()
)

fun List<QuoteResponse>.toDomain(): List<Quote> = map { it.toDomain() }
