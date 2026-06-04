package com.example.gruya.data.remote.dtos.response

data class HelpRequestResponse (
    val id: Int,
    val providerId: Int,
    val providerName: String,
    val etaMinutes: Int
)