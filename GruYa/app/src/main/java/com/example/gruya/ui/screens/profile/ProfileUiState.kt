package com.example.gruya.ui.screens.profile

import com.example.gruya.data.remote.dtos.response.UserResponse

data class ProfileUiState (
    val isLoading: Boolean = false,
    val error: String? = null,

    val user: UserResponse? = null,

    val firstname: String= "",
    val lastname: String= "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,

    val isEditing: Boolean = false,
    val isSaving: Boolean = false
)
