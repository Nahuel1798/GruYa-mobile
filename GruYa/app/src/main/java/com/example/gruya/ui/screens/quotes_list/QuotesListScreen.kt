package com.example.gruya.ui.screens.quotes_list

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    if (acceptedQuote != null) {
        ActiveServiceTrackingContent(
            quote = acceptedQuote,
            uiState = uiState,
            onNavigateBack = onNavigateBack
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Respuestas",
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
                    }
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
                        QuotesListContent(
                            quotes = uiState.quotes,
                            actionLoading = uiState.actionLoading,
                            onAccept = viewModel::accept,
                            onReject = viewModel::reject
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveServiceTrackingContent(
    quote: Quote,
    uiState: QuotesListUiState,
    onNavigateBack: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val assistance = quote.assistance
    val status = assistance.status
    
    val showProviderToOrigin = status == AssistanceStatus.EN_CAMINO_AL_CLIENTE
    val showProviderToDestination = status == AssistanceStatus.EN_CAMINO_AL_DESTINO
    val isTracking = uiState.trackingState is TrackingState.Tracking || 
                     uiState.trackingState is TrackingState.Connected

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Servicio en curso") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        sheetPeekHeight = 160.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 8.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Provider Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = quote.providerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assistance.serviceType.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(Icons.Default.LocationOn, "Origen", uiState.originAddress ?: "Cargando...")
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(
                    Icons.Default.LocationOn, 
                    "Destino", 
                    if (assistance.destination.latitude == 0.0) "No especificado" else uiState.destinationAddress ?: "Cargando..."
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Status Message
                TrackingStatusMessage(
                    status = status,
                    trackingState = uiState.trackingState,
                    distanceKm = uiState.distanceKm,
                    etaMinutes = uiState.etaMinutes
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            TrackingMap(
                origin = assistance.origin,
                destination = assistance.destination,
                routeGeometry = if (showProviderToDestination) null else assistance.routeGeometry,
                providerLocation = uiState.providerLocation,
                providerToOriginRoute = if (showProviderToOrigin) uiState.providerToOriginRoute else null,
                providerToDestinationRoute = if (showProviderToDestination) uiState.providerToDestinationRoute else null,
                isTracking = isTracking,
                isProvider = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TrackingStatusMessage(
    status: AssistanceStatus,
    trackingState: TrackingState,
    distanceKm: Double?,
    etaMinutes: Double?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        when (trackingState) {
            is TrackingState.Connected, is TrackingState.Tracking -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (status) {
                            AssistanceStatus.EN_CAMINO_AL_CLIENTE -> "Proveedor en camino"
                            AssistanceStatus.EN_ORIGEN -> "Proveedor en el lugar"
                            AssistanceStatus.EN_CAMINO_AL_DESTINO -> "Viajando al destino"
                            else -> status.displayName
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (distanceKm != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.1f km · ~%.0f min".format(distanceKm, etaMinutes ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is TrackingState.Disconnected -> {
                Text(
                    text = "Proveedor desconectado",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            is TrackingState.Error -> {
                Text(
                    text = "Error de conexión: ${trackingState.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Text(
                    text = status.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
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
                text = value,
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
        QuoteStatus.COMPLETADO -> "Completada" to Color(0xFF3B82F6)
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
