package com.example.gruya.ui.screens.request_assistance

import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Vehicle

data class RequestAssistanceUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: Int? = null,
    val selectedIssueType: IssueType? = null,
    val location: Pair<Double, Double>? = null,
    val address: String? = null,
    val addressQuery: String = "",
    val destinationLocation: Pair<Double, Double>? = null,
    val destinationAddress: String? = null,
    val destinationAddressQuery: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() = selectedVehicleId != null && selectedIssueType != null
                && location != null && destinationLocation != null
}
