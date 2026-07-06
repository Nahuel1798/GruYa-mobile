package com.example.gruya.data.mapper

import com.example.gruya.data.remote.dtos.response.PaymentResponse
import com.example.gruya.domain.model.Payment

fun PaymentResponse.toDomain(): Payment {
    return Payment(
        id = id,
        amount = amount,
        method = method,
        status = status,
        date = date,
        assistanceId = assistanceId
    )
}

fun List<PaymentResponse>.toDomain(): List<Payment> {
    return this.map { it.toDomain() }
}
