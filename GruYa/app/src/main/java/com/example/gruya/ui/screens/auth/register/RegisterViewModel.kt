package com.example.gruya.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.AuthRepository
import com.example.gruya.domain.model.Role
import com.example.gruya.domain.model.ServiceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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
        viewModelScope.launch {
            val result = authRepository.register(
                firstname = _uiState.value.firstname,
                lastname = _uiState.value.lastname,
                email = _uiState.value.email,
                phone = _uiState.value.phone,
                password = _uiState.value.password,
                role = _uiState.value.role)

            _uiState.update { currentValue ->
                currentValue.copy(success = result.isSuccessful)
            }

            if (result.isSuccessful) {
                val authResponse = result.body()!!
                sessionManager.saveJwt(authResponse.token)
                authResponse.user.role?.let { sessionManager.saveRole(it) }
            }
        }

    }

    fun onContinueClick(){
        val s = _uiState.value
        val error = when {
            s.firstname.isEmpty() -> "Ingrese sun nombre"
            s.lastname.isEmpty() -> "Ingrese su apellido"
            s.phone.isEmpty() -> "Ingrese su teléfono"
            s.email.isEmpty() -> "Ingrese su email"
            s.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        _uiState.update { it.copy(step = RegisterStep.RoleSelector) }

    }
}
