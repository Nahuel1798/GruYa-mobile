package com.example.gruya.ui.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gruya.domain.model.Role
import com.example.gruya.ui.components.OptionCard

@Composable
fun RoleSelector(
    uiState: RegisterUiState,
    onRoleSelected: (Role) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(

        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 100.dp),
            text = "Que uso le darás a la app?",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        OptionCard(
            "Necesito auxílio",
            description = "Para conductores con vehículos averiados, neumáticos pinchados " +
                    "o que necesitan asistencia inmediata en carretera.",
            titleColor = Color(0xFF1E3A8A),
            iconTint = Color(0xFF1E3A8A),
            iconContainerColor = Color.LightGray,
            onClick = { onRoleSelected(Role.USER) }
        )
        OptionCard(
            "Soy Proveedor",
            description = "Para auxilios y profesionales del servicio que buscan aceptar " +
                    "trabajos cercanos y hacer crecer su negocio.",
            titleColor = Color(0xFFF59E0B),
            iconTint = Color(0xFFF59E0B),
            iconContainerColor = Color.LightGray,
            onClick = { onRoleSelected(Role.PROVIDER) }
        )

        Button(
            onClick = {
                onConfirm()
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
