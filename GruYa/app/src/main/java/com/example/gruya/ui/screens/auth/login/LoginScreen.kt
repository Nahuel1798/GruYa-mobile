package com.example.gruya.ui.screens.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.gruya.ui.theme.GruYaTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gruya.MainNavigationSuite
import com.example.gruya.ui.navigation.AppDest

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {

    val loginUiState by viewModel.uiState.collectAsState()

    LaunchedEffect(loginUiState.success) {
        if (loginUiState.success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF2FF),
                        Color(0xFFDCE7FF)
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
                        .background(Color(0xFFE8EDFF)),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Logo GruYa",

                        tint = Color(0xFF003D9B),

                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "GruYa",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF003D9B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Accede para solicitar asistencia vial",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                // EMAIL
                OutlinedTextField(
                    value = loginUiState.email,

                    onValueChange = {
                        viewModel.onEmailChanged(it)
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
                                viewModel.onPasswordVisibilityClick(
                                    !loginUiState.passwordVisible
                                )
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (loginUiState.passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,

                                contentDescription = "Mostrar contraseña"
                            )
                        }
                    },

                    visualTransformation =
                        if (loginUiState.passwordVisible)
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

                // ERROR
                if (loginUiState.error.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = loginUiState.error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // BOTON LOGIN
                Button(
                    onClick = {
                        viewModel.onLoginButtonClick()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF003D9B)
                    )
                ) {

                    if (loginUiState.loading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                    } else {

                        Text(
                            text = "Iniciar Sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val backstack = rememberNavBackStack(
                    AppDest.Register
                )
                NavDisplay(
                    backStack = backstack,
                    entryProvider = entryProvider {
                        entry<AppDest.Register> {
                            MainNavigationSuite(onLogout = {
                                backstack.clear()
                                backstack.add(AppDest.Register)
                            })
                        }
                    }
                )
                TextButton(
                    onClick = {  }
                ) {

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = Color(0xFF003D9B)
                    )
                }
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
            onLoginSuccess = {}
        )
    }
}