package com.example.gruya.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.dtos.request.UpdateUserRequest
import com.example.gruya.data.repository.AuthRepository
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.domain.model.Role
import com.example.gruya.domain.model.ServiceType
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
    private val sessionManager: SessionManager,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val role = sessionManager.getRole()
        val isProvider = role == Role.PROVIDER
        _uiState.update { it.copy(isProvider = isProvider) }
        loadAll()
    }

    fun loadAll() {
        val isProvider = sessionManager.getRole() == Role.PROVIDER
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isProvider = isProvider,
                    isLoading = it.user == null,
                    isLoadingProvider = isProvider && it.providerProfile == null,
                    error = null,
                    providerError = null
                )
            }

            // Launch both in parallel
            launch { loadUserInternal() }
            if (isProvider) {
                launch { loadProviderProfileInternal() }
            }
        }
    }

    private suspend fun loadUserInternal() {
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
                        isLoading = false,
                        isProvider = userResponse.role == Role.PROVIDER
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

    private suspend fun loadProviderProfileInternal() {
        try {
            val result = providerRepository.getMyProfile()
            result.onSuccess { profile ->
                Log.d("ProfileVM", "Provider profile loaded: ${profile.companyName}")
                _uiState.update {
                    it.copy(
                        providerProfile = profile,
                        isLoadingProvider = false
                    )
                }
            }.onFailure { error ->
                Log.e("ProfileVM", "Error loading provider profile", error)
                _uiState.update {
                    it.copy(
                        isLoadingProvider = false,
                        providerError = error.message ?: "Error al cargar perfil de proveedor"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileVM", "Exception loading provider profile", e)
            _uiState.update {
                it.copy(
                    isLoadingProvider = false,
                    providerError = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.user == null,
                    error = null
                )
            }
            loadUserInternal()
        }
    }

    fun loadProviderProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingProvider = it.providerProfile == null,
                    providerError = null
                )
            }
            loadProviderProfileInternal()
        }
    }

    fun updateProviderProfile(
        companyName: String,
        address: String,
        serviceType: ServiceType,
        description: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingProvider = true,
                    providerError = null
                )
            }

            try {
                val request = com.example.gruya.data.remote.dtos.request.UpdateProviderProfileRequest(
                    serviceType = serviceType,
                    companyName = companyName,
                    address = address,
                    description = description,
                    location = null
                )
                val result = providerRepository.updateProfile(request)

                result.onSuccess { updatedProfile ->
                    Log.d("ProfileVM", "Provider profile updated: ${updatedProfile.companyName}")
                    _uiState.update {
                        it.copy(
                            providerProfile = updatedProfile,
                            isSavingProvider = false,
                            isEditingProvider = false
                        )
                    }
                }.onFailure { error ->
                    Log.e("ProfileVM", "Error updating provider profile", error)
                    _uiState.update {
                        it.copy(
                            isSavingProvider = false,
                            providerError = error.message ?: "Error al actualizar perfil"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Exception updating provider profile", e)
                _uiState.update {
                    it.copy(
                        isSavingProvider = false,
                        providerError = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun startEditProvider() {
        val profile = _uiState.value.providerProfile ?: return
        _uiState.update {
            it.copy(
                isEditingProvider = true,
                providerCompanyName = profile.companyName,
                providerAddress = profile.address,
                providerServiceType = profile.serviceType
            )
        }
    }

    fun cancelProviderEdit() {
        _uiState.update {
            it.copy(
                isEditingProvider = false,
                providerError = null
            )
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

        _uiState.value = ProfileUiState()
    }

    fun onOpenPasswordDialog() {
        _uiState.update { it.copy(isPasswordDialogOpen = true, passwordError = null, passwordSuccess = false, oldPassword = "", newPassword = "", confirmPassword = "") }
    }

    fun onClosePasswordDialog() {
        _uiState.update { it.copy(isPasswordDialogOpen = false) }
    }

    fun onOldPasswordChange(value: String) {
        _uiState.update { it.copy(oldPassword = value) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun updatePassword() {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(passwordError = "Las contraseñas no coinciden") }
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.update { it.copy(passwordError = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPassword = true, passwordError = null) }
            val result = authRepository.resetPassword(state.oldPassword, state.newPassword)
            result.onSuccess {
                _uiState.update { it.copy(isUpdatingPassword = false, passwordSuccess = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isUpdatingPassword = false, passwordError = error.message ?: "Error al cambiar la contraseña") }
            }
        }
    }
}
