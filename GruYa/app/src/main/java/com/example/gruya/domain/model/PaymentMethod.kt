package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class PaymentMethod {
    @SerializedName("Efectivo")
    EFECTIVO,
    @SerializedName("MercadoPago")
    MERCADOPAGO,
    @SerializedName("Tarjeta")
    TARJETA
}
