package com.example.gruya.data.remote.dtos.response

import com.google.gson.annotations.SerializedName

data class ProviderLocationResponse (
    val id: Int,
    val userId: Int,
    val companyName: String = "",
    val description: String = "",
    val phone: String = "",
    val serviceType: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = false
)