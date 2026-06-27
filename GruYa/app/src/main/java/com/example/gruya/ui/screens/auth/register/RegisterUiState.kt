package com.example.gruya.ui.screens.auth.register

import com.example.gruya.domain.model.Role
import com.example.gruya.domain.model.ServiceType

data class RegisterUiState(

    val step: RegisterStep = RegisterStep.RoleSelector,

    val firstname: String = "",
    val lastname: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,

    val role: Role = Role.USER,
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String = ""
)

enum class RegisterStep {
    Form,
    RoleSelector
}
