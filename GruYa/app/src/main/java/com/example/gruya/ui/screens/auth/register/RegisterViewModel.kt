package com.example.gruya.ui.screens.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.AuthRepository
import com.example.gruya.domain.model.Role
import com.example.gruya.domain.model.ServiceType
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onFirstNameChanged(value: String) {
        _uiState.update { it.copy(firstname = value, firstnameError = null, error = "") }
    }

    fun onLastNameChanged(value: String) {
        _uiState.update { it.copy(lastname = value, lastnameError = null, error = "") }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value, phoneError = null, error = "") }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, error = "") }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, error = "") }
    }

    fun onPasswordVisibilityChanged(value: Boolean) {
        _uiState.update { it.copy(passwordVisible = value) }
    }

    fun onRoleChanged(value: Role) {
        _uiState.update { it.copy(role = value) }
    }

    fun onRegisterClick() {
        val s = _uiState.value

        val firstnameError = when {
            s.firstname.isBlank() -> "Ingrese su nombre"
            !s.firstname.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo debe contener letras"
            else -> null
        }
        val lastnameError = when {
            s.lastname.isBlank() -> "Ingrese su apellido"
            !s.lastname.all { it.isLetter() || it.isWhitespace() } -> "El apellido solo debe contener letras"
            else -> null
        }
        val phoneError = if (s.phone.isBlank()) "Ingrese su teléfono" else null
        val emailError = when {
            s.email.isBlank() -> "Ingrese su email"
            !Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> "Email inválido"
            else -> null
        }
        val passwordError = when {
            s.password.isBlank() -> "Ingrese una contraseña"
            s.password.length < 8 -> "La contraseña no puede tener menos de 8 caracteres"
            else -> null
        }

        if (firstnameError != null || lastnameError != null || phoneError != null || emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    firstnameError = firstnameError,
                    lastnameError = lastnameError,
                    phoneError = phoneError,
                    emailError = emailError,
                    passwordError = passwordError,
                    error = "Por favor, complete los campos correctamente"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    error = "",
                    firstnameError = null,
                    lastnameError = null,
                    phoneError = null,
                    emailError = null,
                    passwordError = null
                )
            }
            try {
                val fcmToken = FirebaseMessaging.getInstance().getToken().await()

                val result = authRepository.register(
                    firstname = _uiState.value.firstname,
                    lastname = _uiState.value.lastname,
                    email = _uiState.value.email,
                    phone = _uiState.value.phone,
                    password = _uiState.value.password,
                    role = _uiState.value.role,
                    fcmToken = fcmToken
                )

                result.fold(
                    onSuccess = { authResponse ->
                        sessionManager.saveJwt(authResponse.token)
                        authResponse.user.role?.let { sessionManager.saveRole(it) }
                        _uiState.update { it.copy(success = true, loading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message ?: "Error al registrarse", loading = false) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error de conexión. Verificá tu conexión a internet", loading = false) }
            }
        }
    }

    fun onContinueClick(){
        val s = _uiState.value
        if (s.role == Role.USER || s.role == Role.PROVIDER) {
            _uiState.update { it.copy(step = RegisterStep.Form, error = "") }
        } else {
            _uiState.update { it.copy(error = "Seleccioná un rol para continuar") }
        }
    }

    fun onBackToRoleSelection() {
        _uiState.update { it.copy(step = RegisterStep.RoleSelector, error = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(error = "") }
    }
}
