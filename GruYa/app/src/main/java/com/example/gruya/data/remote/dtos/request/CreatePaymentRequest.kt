package com.example.gruya.data.remote.dtos.request

import com.example.gruya.domain.model.PaymentMethod

data class CreatePaymentRequest(
    val amount: Double,
    val method: PaymentMethod
)