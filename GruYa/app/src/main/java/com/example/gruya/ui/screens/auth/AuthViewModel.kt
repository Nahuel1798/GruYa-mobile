package com.example.gruya.ui.screens.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)

    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onLoginSuccess() {
        _isLoggedIn.value = true
    }
    fun logout() {
        _isLoggedIn.value = false
    }
}