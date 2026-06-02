package com.example.gruya.ui.screens.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.dtos.request.UpdateUserRequest
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

    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val token = sessionManager.getJwt()
                if (token.isEmpty()) throw Exception("Token vacío")
                
                val request = UpdateUserRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone
                )

                val result = authRepository.editProfile(token, request)

                result.onSuccess { updatedUser ->
                    Log.d("ProfileVM", "Perfil actualizado correctamente")
                    _uiState.update { currentState ->
                        // Si el servidor no devolvió el usuario, usamos los datos que enviamos
                        // Usamos copy solo si el usuario actual existe para evitar el NPE con 'role'
                        val newUser = updatedUser ?: currentState.user?.let { current ->
                            current.copy(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                phone = phone
                            )
                        }
                        
                        currentState.copy(
                            isLoading = false,
                            user = newUser,
                            name = "${newUser?.firstName ?: firstName} ${newUser?.lastName ?: lastName}",
                            email = newUser?.email ?: email
                        )
                    }
                }.onFailure { error ->
                    Log.e("ProfileVM", "Error actualizando perfil", error)
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Error al actualizar")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Excepción en updateProfile", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "Error inesperado (${e.javaClass.simpleName}): ${e.message}")
                }
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
