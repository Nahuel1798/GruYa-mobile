package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class PaymentStatus {

    @SerializedName("Pendiente")
    PENDIENTE,
    @SerializedName("Pagado")
    PAGADO,
    @SerializedName("Fallido")
    FALLIDO

}
