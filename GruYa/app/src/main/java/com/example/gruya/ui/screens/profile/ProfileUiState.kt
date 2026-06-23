package com.example.gruya.ui.screens.profile

import com.example.gruya.data.remote.dtos.response.UserResponse
import com.example.gruya.domain.model.ProviderProfile
import com.example.gruya.domain.model.ServiceType

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val user: UserResponse? = null,

    val firstname: String = "",
    val lastname: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,

    val isProvider: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,

    // Provider profile fields
    val providerProfile: ProviderProfile? = null,
    val isLoadingProvider: Boolean = false,
    val isEditingProvider: Boolean = false,
    val isSavingProvider: Boolean = false,
    val providerError: String? = null,

    // Editable provider fields
    val providerCompanyName: String = "",
    val providerAddress: String = "",
    val providerServiceType: ServiceType = ServiceType.AUXILIO,

    // Password change fields
    val isPasswordDialogOpen: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isUpdatingPassword: Boolean = false,
    val passwordError: String? = null,
    val passwordSuccess: Boolean = false
)
