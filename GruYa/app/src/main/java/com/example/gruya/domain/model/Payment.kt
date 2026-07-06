package com.example.gruya.domain.model

data class Payment (
    val id: Int,
    val amount: Double,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val date: String,
    val assistanceId: Int
)