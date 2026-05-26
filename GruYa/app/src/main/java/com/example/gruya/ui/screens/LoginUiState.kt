package com.example.gruya.ui.screens

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val success: Boolean = false
)
