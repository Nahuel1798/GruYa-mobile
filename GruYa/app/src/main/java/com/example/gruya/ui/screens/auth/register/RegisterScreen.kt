package com.example.gruya.ui.screens.auth.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    actionColor = MaterialTheme.colorScheme.error
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState.step) {
                RegisterStep.RoleSelector -> RoleSelector(
                    uiState = uiState,
                    onConfirm = viewModel::onContinueClick,
                    onRoleSelected = viewModel::onRoleChanged
                )

                RegisterStep.Form -> RegisterForm(
                    uiState = uiState,
                    onContinue = viewModel::onRegisterClick,
                    onFirstNameChanged = viewModel::onFirstNameChanged,
                    onLastNameChanged = viewModel::onLastNameChanged,
                    onPhoneChanged = viewModel::onPhoneChanged,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onPasswordVisibilityChanged = viewModel::onPasswordVisibilityChanged
                )
            }
        }
    }
}
