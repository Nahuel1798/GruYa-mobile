package com.example.gruya.data.remote.dtos.response

data class NotificationResponse(
    val id: Int,
    val type: String,
    val title: String,
    val body: String,
    val dataJson: String?,
    val sentAt: String,
    val readAt: String?,
    val assistanceId: Int?
)
