package com.example.gruya.ui.screens

data class RegisterUiState(

    val name: String = "",

    val phone: String = "",

    val email: String = "",

    val password: String = "",

    val passwordVisible: Boolean = false,

    val loading: Boolean = false,

    val success: Boolean = false,

    val error: String = ""
)