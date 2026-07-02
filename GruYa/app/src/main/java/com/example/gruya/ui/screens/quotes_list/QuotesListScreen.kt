package com.example.gruya.ui.screens.quotes_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
                title = {
                    Column {
                        Text(
                            "Servicio en curso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assistance.serviceType.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        sheetPeekHeight = 180.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 12.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Provider Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProviderAvatar(name = quote.providerName, size = 56.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = quote.providerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TrackingStatusBadge(status = status, trackingState = uiState.trackingState)
                    }
                    
                    // Quick Action Buttons
                    Row {
                        IconButton(
                            onClick = { /* TODO: Call */ },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { /* TODO: Chat */ },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Mensaje", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Trip Info Card
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(Icons.Default.LocationOn, "Recogida", uiState.originAddress ?: "Cargando...")
                        
                        Box(modifier = Modifier.padding(start = 9.dp, top = 4.dp, bottom = 4.dp)) {
                            VerticalDivider(
                                modifier = Modifier.height(20.dp),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        InfoRow(
                            Icons.Default.Navigation,
                            "Destino",
                            if (assistance.destination.latitude == 0.0) "Sin destino fijo" else uiState.destinationAddress ?: "Cargando..."
                        )
                    }
                }

                if (uiState.distanceKm != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(label = "Distancia", value = "%.1f km".format(uiState.distanceKm))
                        VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp)
                        MetricItem(label = "Llegada", value = "~%.0f min".format(uiState.etaMinutes ?: 0.0))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
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
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TrackingStatusBadge(status: AssistanceStatus, trackingState: TrackingState) {
    val text = when (trackingState) {
        is TrackingState.Connected, is TrackingState.Tracking -> {
            when (status) {
                AssistanceStatus.EN_CAMINO_AL_CLIENTE -> "En camino hacia ti"
                AssistanceStatus.EN_ORIGEN -> "En el lugar"
                AssistanceStatus.EN_CAMINO_AL_DESTINO -> "Viajando al destino"
                else -> status.displayName
            }
        }
        is TrackingState.Disconnected -> "Conexión perdida"
        is TrackingState.Error -> "Error de rastreo"
        else -> status.displayName
    }
    
    val color = when (trackingState) {
        is TrackingState.Disconnected, is TrackingState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProviderAvatar(name: String, size: androidx.compose.ui.unit.Dp) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Box(
        modifier
            .width(thickness)
            .background(color)
    )
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Status Indicator Color Bar
            val indicatorColor = when (quote.status) {
                QuoteStatus.PENDIENTE -> MaterialTheme.colorScheme.primary
                QuoteStatus.ACEPTADA -> Color(0xFF22C55E)
                QuoteStatus.COMPLETADO -> Color(0xFF3B82F6)
                QuoteStatus.RECHAZADA -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(indicatorColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProviderAvatar(name = quote.providerName, size = 40.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = quote.providerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = quote.createdAt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    QuoteStatusBadge(status = quote.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Presupuesto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = priceFormatted,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (quote.status == QuoteStatus.PENDIENTE) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onReject(quote.id) },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isActionLoading,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                            ) {
                                if (isActionLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Rechazar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { onAccept(quote.id) },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isActionLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                            ) {
                                if (isActionLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Text("Aceptar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
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
