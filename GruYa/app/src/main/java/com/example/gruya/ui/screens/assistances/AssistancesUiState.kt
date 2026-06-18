package com.example.gruya.ui.screens.assistances

import com.example.gruya.domain.model.Assistance

data class AssistancesUiState(
    val activeAssistance: Assistance? = null,
    val assistances: List<Assistance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
