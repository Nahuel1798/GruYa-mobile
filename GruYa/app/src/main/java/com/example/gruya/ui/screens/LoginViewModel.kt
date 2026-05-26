package com.example.gruya.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel: ViewModel() {
    private val authRepository = AuthRepository()
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
    fun onLoginButtonClick(){
        viewModelScope.launch {
            val result = authRepository.login(_uiState.value.email, _uiState.value.password)
            _uiState.update { currentValue ->
                currentValue.copy(success = result)
            }
        }
    }

    fun onPasswordVisibilityClick(passwordVisible: Boolean) {
        _uiState.update { curentState ->
            curentState.copy(passwordVisible = passwordVisible)
        }
    }

}