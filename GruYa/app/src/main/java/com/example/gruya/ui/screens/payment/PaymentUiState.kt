package com.example.gruya.ui.screens.payment

import com.example.gruya.domain.model.Payment
import com.example.gruya.domain.model.PaymentMethod

data class PaymentUiState(
    val assistanceId: Int = 0,
    val amount: Double = 0.0,
    val selectedMethod: PaymentMethod? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val payment: Payment? = null
)

