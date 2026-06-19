package com.example.gruya.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String){
        _uiState.update { currentState ->
            currentState.copy(email = email)
        }
    }
    fun onPasswordChanged(password: String){
        _uiState.update { currentState ->
            currentState.copy(password = password)
        }
    }
    fun onLoginButtonClick() {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().getToken().await()

                val result = authRepository.login(
                    _uiState.value.email,
                    _uiState.value.password,
                    fcmToken
                )
                if (result.isSuccessful) {
                    val authResponse = result.body()!!
                    sessionManager.saveJwt(authResponse.token)
                    authResponse.user.role?.let { sessionManager.saveRole(it) }
                    _uiState.update { it.copy(success = true) }
                } else {
                    _uiState.update {
                        it.copy(error = "Email o contraseña incorrectos")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error de conexión. Verificá tu conexión a internet")
                }
            }
        }
    }

    fun onPasswordVisibilityClick(passwordVisible: Boolean) {
        _uiState.update { curentState ->
            curentState.copy(passwordVisible = passwordVisible)
        }
    }
}
