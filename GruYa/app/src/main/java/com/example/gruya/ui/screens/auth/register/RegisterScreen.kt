package com.example.gruya.ui.screens.auth.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gruya.domain.model.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: (Role) -> Unit
){
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.success) {
        if (uiState.success) onRegisterSuccess(uiState.role)
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error.isNotEmpty()) {
            val snackbarJob = launch {
                snackbarHostState.showSnackbar(
                    message = uiState.error,
                    duration = SnackbarDuration.Indefinite
                )
            }
            delay(10000)
            snackbarJob.cancel()
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState.step) {
                RegisterStep.Form -> RegisterForm(
                    uiState = uiState,
                    onContinue = viewModel::onContinueClick,
                    onFirstNameChanged = viewModel::onFirstNameChanged,
                    onLastNameChanged = viewModel::onLastNameChanged,
                    onPhoneChanged = viewModel::onPhoneChanged,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onPasswordVisibilityChanged = viewModel::onPasswordVisibilityChanged
                )

                RegisterStep.RoleSelector -> RoleSelector(
                    uiState = uiState,
                    onConfirm = viewModel::onRegisterClick,
                    onRoleSelected = viewModel::onRoleChanged
                )
            }
        }
    }
}
