package com.example.gruya.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.dtos.request.UpdateUserRequest
import com.example.gruya.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
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
                val result = authRepository.getProfile()

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
                val request = UpdateUserRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone
                )

                val result = authRepository.editProfile(request)

                result.onSuccess { updatedUser ->
                    Log.d("ProfileVM", "Perfil actualizado correctamente")
                    _uiState.update { currentState ->
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
