package com.example.gruya.ui.screens.quotes_list

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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.Quote
import com.example.gruya.domain.model.QuoteStatus
import com.example.gruya.domain.model.TrackingState
import com.example.gruya.domain.model.displayName
import com.example.gruya.ui.components.TrackingMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesListScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuotesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val acceptedQuote = remember(uiState.quotes) {
        uiState.quotes.find { it.status == QuoteStatus.ACEPTADA }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (acceptedQuote != null) "Servicio en curso" else "Respuestas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (acceptedQuote != null) Color.Transparent else MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.quotes.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.quotes.isEmpty() -> {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.quotes.isEmpty() -> {
                    Text(
                        text = "No hay presupuestos disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // Background Map
                    val firstAssistance = uiState.quotes.firstOrNull()?.assistance
                    if (firstAssistance != null) {
                        val assistance = acceptedQuote?.assistance ?: firstAssistance
                        TrackingMap(
                            origin = assistance.origin,
                            destination = assistance.destination,
                            routeGeometry = assistance.routeGeometry,
                            providerLocation = uiState.providerLocation,
                            providerToOriginRoute = uiState.providerToOriginRoute,
                            isTracking = uiState.trackingState is TrackingState.Tracking || 
                                         uiState.trackingState is TrackingState.Connected,
                            isProvider = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (acceptedQuote == null) {
                        // Full screen list hiding the map
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            QuotesListContent(
                                quotes = uiState.quotes,
                                actionLoading = uiState.actionLoading,
                                onAccept = viewModel::accept,
                                onReject = viewModel::reject
                            )
                        }
                    } else {
                        // Active Service View at the bottom
                        ActiveServiceCard(
                            quote = acceptedQuote,
                            originAddress = uiState.originAddress,
                            destinationAddress = uiState.destinationAddress,
                            distanceKm = uiState.distanceKm,
                            etaMinutes = uiState.etaMinutes,
                            trackingState = uiState.trackingState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveServiceCard(
    quote: Quote,
    originAddress: String?,
    destinationAddress: String?,
    distanceKm: Double?,
    etaMinutes: Double?,
    trackingState: TrackingState,
    modifier: Modifier = Modifier
) {
    val assistance = quote.assistance
    val isTracking = trackingState is TrackingState.Tracking || trackingState is TrackingState.Connected

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Status and Provider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = assistance.status.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = quote.providerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isTracking && distanceKm != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%.1f km".format(distanceKm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "~%.0f min".format(etaMinutes ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Addresses
            AddressRow(
                icon = Icons.Default.LocationOn,
                label = "Origen",
                address = originAddress ?: "Cargando..."
            )
            Spacer(modifier = Modifier.height(12.dp))
            AddressRow(
                icon = Icons.Default.LocationOn,
                label = "Destino",
                address = if (assistance.destination.latitude == 0.0) "No especificado" else destinationAddress ?: "Cargando..."
            )
        }
    }
}

@Composable
private fun AddressRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    address: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuotesListContent(
    quotes: List<Quote>,
    actionLoading: Int?,
    onAccept: (Int) -> Unit,
    onReject: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(quotes, key = { it.id }) { quote ->
            QuoteCard(
                quote = quote,
                actionLoading = actionLoading,
                onAccept = onAccept,
                onReject = onReject
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    actionLoading: Int?,
    onAccept: (Int) -> Unit,
    onReject: (Int) -> Unit
) {
    val isActionLoading = actionLoading == quote.id
    val priceFormatted = "$%.2f".format(quote.price)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                QuoteStatusBadge(status = quote.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Provider name
            Text(
                text = quote.providerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            // Created at
            Text(
                text = quote.createdAt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Accept / Reject buttons (only for PENDIENTE)
            if (quote.status == QuoteStatus.PENDIENTE) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReject(quote.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isActionLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Rechazar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onAccept(quote.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isActionLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Aceptar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteStatusBadge(status: QuoteStatus) {
    val (label, badgeColor) = when (status) {
        QuoteStatus.PENDIENTE -> "Pendiente" to MaterialTheme.colorScheme.primary
        QuoteStatus.ACEPTADA -> "Aceptada" to Color(0xFF22C55E)
        QuoteStatus.RECHAZADA -> "Rechazada" to MaterialTheme.colorScheme.error
        QuoteStatus.CANCELADA -> "Cancelada" to MaterialTheme.colorScheme.onSurfaceVariant
        QuoteStatus.EXPIRADA -> "Expirada" to MaterialTheme.colorScheme.onSurfaceVariant
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
