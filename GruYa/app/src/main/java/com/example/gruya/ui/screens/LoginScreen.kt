package com.example.gruya.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.gruya.ui.theme.GruYaTheme

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
       Card(
           modifier = Modifier
               .fillMaxWidth()
               .padding(24.dp)
       ) {
           Column(
               modifier = Modifier.padding(24.dp),
               verticalArrangement = Arrangement.spacedBy(16.dp)
           ) {
               Text(
                   text = "GruYa Login",
                   style = MaterialTheme.typography.headlineMedium
               )

               OutlinedTextField(
                   value = email,
                   onValueChange = {
                       email = it
                   },
                   label = {
                       Text("Email")
                   },
                   modifier = Modifier.fillMaxWidth()
               )

               OutlinedTextField(
                   value = password,
                   onValueChange = {
                       password = it
                   },
                   label = {
                       Text("Contraseña")
                   },
                   visualTransformation = PasswordVisualTransformation(),
                   modifier = Modifier.fillMaxWidth()
               )

               Button(onClick = {
                   onLoginClick(email,password)
               },
                   modifier = Modifier.fillMaxWidth()
               ){
                   Text("Iniciar sesion")
               }
           }
       }
    }
}

@PreviewScreenSizes
@Composable
private fun LoginScreenPreview() {
    GruYaTheme {
        LoginScreen(onLoginClick = { _, _ -> })
    }
}