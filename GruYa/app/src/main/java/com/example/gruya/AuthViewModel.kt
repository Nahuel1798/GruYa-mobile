package com.example.gruya

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.AuthResponseInterceptor
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.data.service.AuthService
import com.example.gruya.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authService: AuthService,
    private val providerRepository: ProviderRepository,
    private val authEventBus: AuthEventBus,
    private val authResponseInterceptor: AuthResponseInterceptor
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _isLoggedIn = MutableStateFlow(false)
    private val _isCheckingToken = MutableStateFlow(false)
    private val _currentRole = MutableStateFlow<Role?>(null)
    private val _isProviderProfileComplete = MutableStateFlow<Boolean?>(null)
    private val _providerProfileError = MutableStateFlow<String?>(null)

    init {
        val token = sessionManager.getJwt()
        if (token.isNotBlank()) {
            _currentRole.value = sessionManager.getRole()
            _isCheckingToken.value = true
            viewModelScope.launch {
                try {
                    val response = authService.validateToken()
                    if (response.isSuccessful) {
                        authResponseInterceptor.resetLoggedOutFlag()
                        _isLoggedIn.value = true
                        // After successful validation, also check provider profile
                        // if the user is a provider. The gate keeps the spinner
                        // until both checks pass.
                        if (_currentRole.value == Role.PROVIDER) {
                            checkProviderProfile()
                        } else {
                            _isCheckingToken.value = false
                        }
                    } else {
                        sessionManager.clearSession()
                        _isCheckingToken.value = false
                    }
                } catch (_: java.io.IOException) {
                    Log.w(TAG, "Network error during token validation — assuming token might still be valid")
                    _isLoggedIn.value = true
                    if (_currentRole.value == Role.PROVIDER) {
                        checkProviderProfile()
                    } else {
                        _isCheckingToken.value = false
                    }
                }
            }
        }
    }

    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    val isCheckingToken: StateFlow<Boolean> = _isCheckingToken.asStateFlow()
    val currentRole: StateFlow<Role?> = _currentRole.asStateFlow()
    val isProviderProfileComplete: StateFlow<Boolean?> = _isProviderProfileComplete.asStateFlow()
    val providerProfileError: StateFlow<String?> = _providerProfileError.asStateFlow()

    val authEvents: SharedFlow<AuthEvent> = authEventBus.events

    fun onLoginSuccess() {
        val token = sessionManager.getJwt()
        if (token.isNotBlank()) {
            _currentRole.value = sessionManager.getRole()
            authResponseInterceptor.resetLoggedOutFlag()
            _isLoggedIn.value = true
            // DO NOT check provider profile here — the calling context handles it.
            // Login flow: LoginScreen kicks off refreshProviderProfile() explicitly.
            // Registration flow: entry<AppDest.ProviderProfile> calls markProviderProfileComplete().
        }
    }

    /**
     * Sets [isProviderProfileComplete] to true optimistically.
     * Used after successful provider profile creation to avoid
     * triggering the Gate 1.5 spinner that would appear if
     * the value were null.
     */
    fun markProviderProfileComplete() {
        _isProviderProfileComplete.value = true
    }

    /**
     * Kicks off an async provider profile check WITHOUT nulling the
     * current [isProviderProfileComplete]. Safe to call while the UI
     * is already past the Gate — no flicker.
     *
     * For the init flow, [checkProviderProfile] is used instead because
     * it gates on [_isCheckingToken].
     */
    fun refreshProviderProfile() {
        viewModelScope.launch {
            providerRepository.getMyProfile()
                .onSuccess { profile ->
                    _isProviderProfileComplete.value = profile != null
                }
                .onFailure { e ->
                    Log.w(TAG, "Provider profile refresh failed", e)
                    // Keep the current value — no flicker
                }
        }
    }

    /**
     * Internal: runs a full profile check from the init flow.
     * Nulls out state and keeps [_isCheckingToken] true until complete,
     * so the UI never flashes the wrong screen.
     */
    private fun checkProviderProfile() {
        viewModelScope.launch {
            _isProviderProfileComplete.value = null
            _providerProfileError.value = null
            providerRepository.getMyProfile()
                .onSuccess { profile ->
                    _isProviderProfileComplete.value = profile != null
                }
                .onFailure { e ->
                    Log.w(TAG, "Provider profile check failed", e)
                    _providerProfileError.value = e.message ?: "Error al verificar perfil"
                    _isProviderProfileComplete.value = null
                }
            _isCheckingToken.value = false
        }
    }

    fun clearSessionOnly() {
        sessionManager.clearSession()
    }

    fun logout() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        _currentRole.value = null
        _isProviderProfileComplete.value = null
        _providerProfileError.value = null
        viewModelScope.launch {
            try {
                authService.logout()
            } catch (_: Exception) {
                // Fire-and-forget: local session cleared regardless
            }
        }
    }
}
