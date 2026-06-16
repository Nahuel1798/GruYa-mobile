package com.example.gruya.ui.screens.quote

import com.example.gruya.data.remote.dtos.response.AssistanceResponse

data class QuoteUiState(
    val assistanceRequest: AssistanceResponse? = null,
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val price: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)
