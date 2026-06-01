package com.example.gruya.ui.screens.profile

import com.example.gruya.data.remote.dtos.response.UserResponse

data class ProfileUiState (
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val user: UserResponse? = null,
    val error: String? = null
)
