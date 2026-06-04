package com.example.gruya.ui.screens.auth.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) onRegisterSuccess()
    }

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

        RegisterStep.ProviderProfile -> ProviderProfile()
    }


}