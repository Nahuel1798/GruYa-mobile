package com.example.gruya.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AuthRepository
import com.example.gruya.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel: ViewModel() {

    private val authRepository = AuthRepository()
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onFirstNameChanged(value: String) {
        _uiState.update { it.copy(firstname = value) }
    }

    fun onLastNameChanged(value: String) {
        _uiState.update { it.copy(lastname = value) }
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

    fun onRoleChanged(value: Role) {
        _uiState.update { it.copy(role = value) }
    }

    fun onRegisterClick() {
        val currentState = _uiState.value

        when {
            currentState.firstname.isBlank() -> {
                _uiState.update { it.copy(error = "Ingrese su nombre") }
            }

            currentState.lastname.isBlank() -> {
                _uiState.update { it.copy(error = "Ingrese su apellido") }
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

    fun onRegisterButtonClick(){
        viewModelScope.launch {
            val result = authRepository.register(_uiState.value.firstname, _uiState.value.lastname,_uiState.value.email,_uiState.value.password,_uiState.value.phone,_uiState.value.role)
            _uiState.update { currentValue ->
                currentValue.copy(success = result)
            }
        }
    }
}