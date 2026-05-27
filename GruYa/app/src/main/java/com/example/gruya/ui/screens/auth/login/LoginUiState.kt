package com.example.gruya.ui.screens.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val error: String = "",
    val success: Boolean = false
)
