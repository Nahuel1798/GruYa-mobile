package com.example.gruya.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.gruya.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val sessionManager = SessionManager(getApplication())
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