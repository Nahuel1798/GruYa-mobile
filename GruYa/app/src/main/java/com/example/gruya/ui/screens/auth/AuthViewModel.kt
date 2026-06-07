package com.example.gruya.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.example.gruya.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)

    init {
        val token = sessionManager.getJwt()
        if (token.isNotBlank()) {
            _isLoggedIn.value = true
        }
    }

    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onLoginSuccess() {
        val token = sessionManager.getJwt()
        if (token.isNotBlank()) {
            _isLoggedIn.value = true
        }
    }
    fun logout() {
        _isLoggedIn.value = false
    }
}
