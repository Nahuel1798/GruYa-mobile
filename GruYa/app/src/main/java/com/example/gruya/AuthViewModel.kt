package com.example.gruya

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.AuthResponseInterceptor
import com.example.gruya.data.service.AuthService
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
    private val authEventBus: AuthEventBus,
    private val authResponseInterceptor: AuthResponseInterceptor
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }
    private val _isLoggedIn = MutableStateFlow(false)
    private val _isCheckingToken = MutableStateFlow(false)

    init {
        val token = sessionManager.getJwt()
        if (token.isNotBlank()) {
            _isCheckingToken.value = true
            viewModelScope.launch {
                try {
                    val response = authService.validateToken()
                    if (response.isSuccessful) {
                        authResponseInterceptor.resetLoggedOutFlag()
                        _isLoggedIn.value = true
                    } else {
                        sessionManager.clearSession()
                    }
                } catch (_: java.io.IOException) {
                    Log.w(TAG, "Network error during token validation — assuming token might still be valid")
                    _isLoggedIn.value = true
                } finally {
                    _isCheckingToken.value = false
                }
            }
        }
    }

    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    val isCheckingToken: StateFlow<Boolean> = _isCheckingToken.asStateFlow()

    val authEvents: SharedFlow<AuthEvent> = authEventBus.events

    fun onLoginSuccess() {
        val token = sessionManager.getJwt()
        if (token.isNotBlank()) {
            authResponseInterceptor.resetLoggedOutFlag()
            _isLoggedIn.value = true
        }
    }

    fun clearSessionOnly() {
        sessionManager.clearSession()
    }

    fun logout() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        viewModelScope.launch {
            try {
                authService.logout()
            } catch (_: Exception) {
                // Fire-and-forget: local session cleared regardless
            }
        }
    }

}
