package com.example.gruya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gruya.ui.theme.GruYaTheme

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, viewModel: LoginViewModel = viewModel()
) {
val loginUiState by viewModel.uiState.collectAsState()
    LaunchedEffect(loginUiState.success) {
        if (loginUiState.success){
            onLoginSuccess()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FF)),

        contentAlignment = Alignment.Center
    ) {

        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),

            shape = RoundedCornerShape(28.dp),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
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
                        .background(Color(0xFFE8EDFF)),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,

                        tint = Color(0xFF003D9B),

                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "GruYa",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF003D9B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Accede para solicitar asistencia vial",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(30.dp))

                // EMAIL
                OutlinedTextField(

                    value = loginUiState.email,

                    onValueChange = { viewModel.onEmailChanged(it) },

                    label = {
                        Text("Correo electrónico")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null
                        )
                    },

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PASSWORD
                OutlinedTextField(

                    value = loginUiState.password,

                    onValueChange = {
                        viewModel.onPasswordChanged(it)
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

                    trailingIcon = {

                        IconButton(

                            onClick = {
                               viewModel.onPasswordVisibilityClick(!loginUiState.passwordVisible)
                            }
                        ) {

                            Icon(

                                imageVector = if (loginUiState.passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,

                                contentDescription = null
                            )
                        }
                    },

                    visualTransformation = if (loginUiState.passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // BOTON LOGIN
                Button(

                    onClick = {
                        viewModel.onLoginButtonClick()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF003D9B)
                    )

                ) {

                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { }
                ) {

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = Color(0xFF003D9B)
                    )
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun LoginScreenPreview() {

    GruYaTheme {

        LoginScreen(
            onLoginSuccess = TODO()
        )
    }
}