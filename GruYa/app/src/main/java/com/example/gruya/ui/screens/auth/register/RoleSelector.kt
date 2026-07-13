package com.example.gruya.ui.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gruya.domain.model.Role
import com.example.gruya.ui.components.OptionCard

@Composable
fun RoleSelector(
    uiState: RegisterUiState,
    onRoleSelected: (Role) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Bienvenido a GruYa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "¿Qué uso le darás a la app?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.semantics { selectableGroup() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OptionCard(
                    title = "Necesito auxilio",
                    description = "Para conductores con vehículos averiados o que necesitan asistencia inmediata.",
                    icon = Icons.Default.CarRepair,
                    iconContentDescription = "Icono de conductor",
                    titleColor = MaterialTheme.colorScheme.primary,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    isSelected = uiState.role == Role.USER,
                    onClick = { onRoleSelected(Role.USER) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OptionCard(
                    title = "Soy Proveedor",
                    description = "Para profesionales que buscan aceptar trabajos y hacer crecer su negocio.",
                    icon = Icons.Default.Engineering,
                    iconContentDescription = "Icono de proveedor",
                    titleColor = MaterialTheme.colorScheme.secondary,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    isSelected = uiState.role == Role.PROVIDER,
                    onClick = { onRoleSelected(Role.PROVIDER) }
                )
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
