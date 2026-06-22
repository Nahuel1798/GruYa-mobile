package com.example.gruya.ui.screens.assistances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.domain.model.Assistance
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.ServiceType
import com.example.gruya.domain.model.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistancesScreen(
    onNavigateToQuotes: (Int) -> Unit = {},
    viewModel: AssistancesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAssistances()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Solicitudes",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null && uiState.assistances.isEmpty() && uiState.activeAssistance == null -> {
                    AssistancesErrorContent(
                        error = uiState.error!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.assistances.isEmpty() && uiState.activeAssistance == null -> {
                    AssistancesEmptyContent(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    AssistancesListContent(
                        activeAssistance = uiState.activeAssistance,
                        assistances = uiState.assistances,
                        onNavigateToQuotes = onNavigateToQuotes,
                        onCancelAssistance = viewModel::cancelActiveAssistance,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistancesListContent(
    activeAssistance: Assistance?,
    assistances: List<Assistance>,
    onNavigateToQuotes: (Int) -> Unit,
    onCancelAssistance: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        activeAssistance?.let {
            item {
                Text(
                    text = "Solicitud Activa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ActiveAssistanceCard(
                    assistance = it,
                    onNavigateToQuotes = onNavigateToQuotes,
                    onCancelAssistance = onCancelAssistance
                )
            }
            item {
                if (assistances.isNotEmpty()) {
                    Text(
                        text = "Historial y otras solicitudes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(assistances.filter { it.id != activeAssistance?.id }, key = { it.id }) { assistance ->
            AssistanceCard(
                assistance = assistance,
                onNavigateToQuotes = onNavigateToQuotes
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ActiveAssistanceCard(
    assistance: Assistance,
    onNavigateToQuotes: (Int) -> Unit,
    onCancelAssistance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconForServiceType(
                            serviceType = assistance.serviceType,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = assistance.issueType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Solicitud en curso",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                StatusBadge(status = assistance.status)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vehicle info in a subtle box
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CarRepair,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${assistance.vehicle.brand} ${assistance.vehicle.model}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = assistance.vehicle.licensePlate,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onNavigateToQuotes(assistance.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Ver detalles y seguimiento",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancelAssistance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Cancelar solicitud",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun AssistanceCard(
    assistance: Assistance,
    onNavigateToQuotes: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconForServiceType(
                        serviceType = assistance.serviceType,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = assistance.issueType.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${assistance.vehicle.brand} ${assistance.vehicle.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = assistance.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateToQuotes(assistance.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Ver respuestas",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun IconForServiceType(
    serviceType: ServiceType,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val icon: ImageVector = when (serviceType) {
        ServiceType.AUXILIO -> Icons.Default.CarRepair
        ServiceType.GOMERIA -> Icons.Default.TireRepair
        ServiceType.MECANICO -> Icons.Default.Build
    }
    Icon(
        imageVector = icon,
        contentDescription = serviceType.displayName,
        modifier = modifier.size(24.dp),
        tint = tint
    )
}

@Composable
private fun StatusBadge(status: AssistanceStatus) {
    val (label, badgeColor) = when (status) {
        AssistanceStatus.PENDIENTE -> "Pendiente" to Color(0xFFF59E0B) // Warning
        AssistanceStatus.EN_PROCESO -> "En Proceso" to Color(0xFFFFB95F) // Primary
        AssistanceStatus.COMPLETADO -> "Completado" to Color(0xFF22C55E) // Success
        AssistanceStatus.CANCELADO -> "Cancelado" to Color(0xFFEF4444)   // Error
    }

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = badgeColor.copy(alpha = 0.15f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = badgeColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AssistancesEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CarRepair,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Todavía no tienes solicitudes de auxilio.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AssistancesErrorContent(
    error: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview(
    name = "AssistancesScreen – Light",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun AssistancesScreenPreview() {
    AssistancesScreen()
}
