package com.example.gruya.ui.screens.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val sessionManager = SessionManager(application)
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val token = sessionManager.getJwt()
                Log.d("ProfileVM", "Token recuperado: ${token.take(10)}...")
                val result = authRepository.getProfile(token)

                result.onSuccess { userResponse ->
                    Log.d("ProfileVM", "Perfil cargado con éxito: ${userResponse.firstName}")
                    _uiState.update {
                        it.copy(
                            user = userResponse,
                            name = "${userResponse.firstName} ${userResponse.lastName}",
                            email = userResponse.email,
                            avatarUrl = userResponse.avatarUrl,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    Log.e("ProfileVM", "Error en el repositorio", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error desconocido"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error recuperando token o ejecutando", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se encontró una sesión activa o error interno"
                    )
                }
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        // Aquí podrías actualizar un estado global para navegar al login
    }
}
