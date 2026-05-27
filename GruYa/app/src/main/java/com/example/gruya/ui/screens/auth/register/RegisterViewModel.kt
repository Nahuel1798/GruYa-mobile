package com.example.gruya.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onPasswordVisibilityChanged(value: Boolean) {
        _uiState.update { it.copy(passwordVisible = value) }
    }

    fun onRegisterClick() {
        val currentState = _uiState.value

        when {
            currentState.name.isBlank() -> {
                _uiState.update { it.copy(error = "Ingrese su nombre") }
            }

            currentState.phone.isBlank() -> {
                _uiState.update { it.copy(error = "Ingrese su telefono") }
            }

            currentState.email.isBlank() -> {
                _uiState.update { it.copy(error = "Ingrese su email") }
            }

            currentState.password.length < 6  -> {
                _uiState.update { it.copy(error = "La cantraseña debe tener al menos 6 caracteres") }
            }
            else -> {
                _uiState.update { it.copy(error = "", loading = true) }
                _uiState.update { it.copy(loading = false, success = true) }
            }
        }
    }
}