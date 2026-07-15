package com.example.gruya.ui.screens.auth.register

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gruya.ui.components.AppTextField
import com.example.gruya.ui.components.BackButton
import com.example.gruya.ui.theme.GruYaTheme
import com.example.gruya.domain.model.Role

@Composable
fun RegisterForm(
    uiState: RegisterUiState,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                )
            )
            .verticalScroll(rememberScrollState()),

        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),

            shape = RoundedCornerShape(30.dp),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // BACK BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    BackButton(onClick = onBack)
                }

                // ICONO
                val roleIcon = when (uiState.role) {
                    Role.USER -> Icons.Default.Person
                    Role.PROVIDER -> Icons.Default.Engineering
                    Role.ADMIN -> Icons.Default.AdminPanelSettings
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = roleIcon,
                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.primary,

                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Crear Cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Registrate para usar GruYa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Banner de Error Visual
                AnimatedVisibility(
                    visible = uiState.error.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // NOMBRE
                AppTextField(
                    value = uiState.firstname,
                    onValueChange = onFirstNameChanged,
                    placeholder = "Nombre",
                    leadingIcon = Icons.Default.Person,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                    isError = uiState.firstnameError != null,
                    errorMessage = uiState.firstnameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // APELLIDO
                AppTextField(
                    value = uiState.lastname,
                    onValueChange = onLastNameChanged,
                    placeholder = "Apellido",
                    leadingIcon = Icons.Default.Person,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                    isError = uiState.lastnameError != null,
                    errorMessage = uiState.lastnameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TELEFONO
                AppTextField(
                    value = uiState.phone,
                    onValueChange = onPhoneChanged,
                    placeholder = "Teléfono",
                    leadingIcon = Icons.Default.Phone,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Phone,
                    isError = uiState.phoneError != null,
                    errorMessage = uiState.phoneError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // EMAIL
                AppTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    placeholder = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PASSWORD
                AppTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    placeholder = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    visualTransformation = if (uiState.passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onPasswordVisibilityChanged(!uiState.passwordVisible)
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (uiState.passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // BOTON REGISTER
                Button(
                    onClick = {
                        onContinue()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {

                    if (uiState.loading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                    } else {
                        Text(
                            text = "Registrarse",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
/*
@PreviewScreenSizes
@Composable
private fun RegisterScreenPreview() {

    GruYaTheme {

        RegisterScreen(
            onRegisterSuccess = {}
        )
    }
}*/