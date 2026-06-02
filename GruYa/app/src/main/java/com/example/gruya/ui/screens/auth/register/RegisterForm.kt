package com.example.gruya.ui.screens.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import com.example.gruya.ui.theme.GruYaTheme

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
) {

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary
    )

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

                // ICONO
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
                        imageVector = Icons.Default.Warning,
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

                Spacer(modifier = Modifier.height(30.dp))

                // NOMBRE
                OutlinedTextField(
                    value = uiState.firstname,

                    onValueChange = {
                        onFirstNameChanged(it)
                    },

                    label = {
                        Text("Nombre")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },

                    colors = textFieldColors,

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp),

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // APELLIDO
                OutlinedTextField(
                    value = uiState.lastname,

                    onValueChange = {
                        onLastNameChanged(it)
                    },

                    label = {
                        Text("Apellido")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },

                    colors = textFieldColors,

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp),

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TELEFONO
                OutlinedTextField(
                    value = uiState.phone,

                    onValueChange = {
                        onPhoneChanged(it)
                    },

                    label = {
                        Text("Teléfono")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null
                        )
                    },

                    colors = textFieldColors,

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp),

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // EMAIL
                OutlinedTextField(
                    value = uiState.email,

                    onValueChange = {
                        onEmailChanged(it)
                    },

                    label = {
                        Text("Correo electrónico")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null
                        )
                    },

                    colors = textFieldColors,

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp),

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PASSWORD
                OutlinedTextField(
                    value = uiState.password,

                    onValueChange = {
                        onPasswordChanged(it)
                    },

                    label = {
                        Text("Contraseña")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null
                        )
                    },

                    colors = textFieldColors,

                    trailingIcon = {

                        IconButton(
                            onClick = {
                                onPasswordVisibilityChanged(
                                    !uiState.passwordVisible
                                )
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (uiState.passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,

                                contentDescription = null
                            )
                        }
                    },

                    visualTransformation =
                        if (uiState.passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp),

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                if (uiState.error.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

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