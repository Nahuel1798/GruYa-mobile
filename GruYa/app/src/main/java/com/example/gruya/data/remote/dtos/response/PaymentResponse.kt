package com.example.gruya.data.remote.dtos.response

import com.example.gruya.domain.model.PaymentMethod
import com.example.gruya.domain.model.PaymentStatus

data class PaymentResponse(
    val id: Int,
    val amount: Double,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val date: String,
    val assistanceId: Int
)