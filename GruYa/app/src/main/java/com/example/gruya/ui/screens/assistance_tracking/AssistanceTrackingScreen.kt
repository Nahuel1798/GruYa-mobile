package com.example.gruya.ui.screens.assistance_tracking

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.ui.components.TrackingMap
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.Payment
import com.example.gruya.domain.model.PaymentMethod
import com.example.gruya.domain.model.PaymentStatus
import com.example.gruya.domain.model.TrackingState
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistanceTrackingScreen(
    assistanceId: Int,
    trackingSessionId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (Int, Double) -> Unit,
    viewModel: AssistanceTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            // Optional: Show some message that tracking won't work without permissions
        }
    }

    LaunchedEffect(uiState.isProvider) {
        if (uiState.isProvider) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            locationPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    LaunchedEffect(assistanceId, trackingSessionId) {
        viewModel.loadAssistance(assistanceId, trackingSessionId)
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        sheetPeekHeight = 160.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 8.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            uiState.assistance?.let { assistance ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    // Header: Status and Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(assistance.status)
                        Text(
                            text = "#${assistance.id}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Client Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${assistance.client.firstName} ${assistance.client.lastName}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = assistance.serviceType.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { /* TODO: Call client */ },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { /* TODO: Chat with client */ },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Mensaje", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ETA and Distance Row
                    if (assistance.status != AssistanceStatus.COMPLETADO && assistance.status != AssistanceStatus.CANCELADO) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem(
                                icon = Icons.Default.AccessTime,
                                value = if (assistance.etaMinutes != null) "${assistance.etaMinutes.toInt()} min" else "--",
                                label = "Llegada"
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            MetricItem(
                                icon = Icons.Default.DirectionsCar,
                                value = if (assistance.distanceKm != null) String.format(Locale.getDefault(), "%.1f km", assistance.distanceKm) else "--",
                                label = "Distancia"
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Address Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "Punto de recogida",
                                value = uiState.originAddress ?: "Cargando...",
                                iconColor = Color(0xFF4CAF50)
                            )
                            
                            Box(modifier = Modifier.padding(start = 9.dp)) {
                                VerticalDivider(
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(2.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            InfoRow(
                                icon = Icons.Default.Flag,
                                label = "Destino final",
                                value = uiState.destinationAddress ?: "Cargando...",
                                iconColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val trackingState = uiState.trackingState
                    val status = assistance.status

                    if (trackingState is TrackingState.Error) {
                        Text(
                            text = trackingState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    when (status) {
                        AssistanceStatus.ACEPTADA -> {
                            if (uiState.isProvider) {
                                ProviderActionButton(
                                    label = "INICIAR VIAJE",
                                    icon = Icons.Default.PlayArrow,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    onClick = { viewModel.startTrip() }
                                )
                            } else {
                                Text(
                                    text = "El proveedor está en camino a tu ubicación",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                )
                            }
                        }
                        AssistanceStatus.EN_CAMINO_AL_CLIENTE -> {
                            if (uiState.isProvider) {
                                ProviderActionButton(
                                    label = "LLEGUÉ AL CLIENTE",
                                    icon = Icons.Default.LocationOn,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    enabled = uiState.isNearOrigin,
                                    onClick = { viewModel.arriveAtOrigin() }
                                )
                                if (!uiState.isNearOrigin && !uiState.isLoading) {
                                    Text(
                                        text = "Debes estar a menos de 300m del origen",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = "El proveedor está en camino a tu ubicación",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                )
                            }
                        }
                        AssistanceStatus.EN_ORIGEN -> {
                            if (uiState.isProvider) {
                                ProviderActionButton(
                                    label = "IR AL DESTINO",
                                    icon = Icons.Default.Flag,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    onClick = { viewModel.headToDestination() }
                                )
                            } else {
                                Text(
                                    text = "El proveedor ha llegado a tu ubicación",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                )
                            }
                        }
                        AssistanceStatus.EN_CAMINO_AL_DESTINO -> {
                            if (uiState.isProvider) {
                                ProviderActionButton(
                                    label = "REALIZAR PAGO",
                                    icon = Icons.Default.CreditCard,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    enabled = uiState.isNearDestination,
                                    onClick = { 
                                        onNavigateToPayment(
                                            assistance.id, 
                                            uiState.acceptedQuote?.price ?: 0.0
                                        ) 
                                    }
                                )
                                if (!uiState.isNearDestination && !uiState.isLoading) {
                                    Text(
                                        text = "Debes estar a menos de 300m del destino",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                if (uiState.payment?.status == PaymentStatus.PAGADO) {
                                    PaidSuccessBadge(uiState.acceptedQuote?.price ?: 0.0)
                                } else if (uiState.isNearDestination) {
                                    ProviderActionButton(
                                        label = "REALIZAR PAGO",
                                        icon = Icons.Default.CreditCard,
                                        isLoading = uiState.isLoading,
                                        isError = false,
                                        onClick = { 
                                            onNavigateToPayment(
                                                assistance.id, 
                                                uiState.acceptedQuote?.price ?: 0.0
                                            ) 
                                        }
                                    )
                                } else {
                                    Text(
                                        text = "El proveedor está en camino al destino",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                        AssistanceStatus.COMPLETADO -> {
                            if (!uiState.isProvider) {
                                if (uiState.payment?.status == PaymentStatus.PAGADO) {
                                    PaidSuccessBadge(uiState.acceptedQuote?.price ?: 0.0)
                                } else {
                                    ProviderActionButton(
                                        label = "REALIZAR PAGO",
                                        icon = Icons.Default.CreditCard,
                                        isLoading = uiState.isLoading,
                                        isError = false,
                                        onClick = { 
                                            onNavigateToPayment(
                                                assistance.id, 
                                                uiState.acceptedQuote?.price ?: 0.0
                                            ) 
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    text = "Servicio completado",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        AssistanceStatus.CANCELADO -> {
                            Text(
                                text = "Servicio cancelado",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        AssistanceStatus.PENDIENTE -> {
                            Text(
                                text = "Esperando asignación...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } ?: Box(Modifier.fillMaxWidth().height(100.dp))
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.assistance == null && uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.assistance == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadAssistance(assistanceId, trackingSessionId) }) {
                        Text("Reintentar")
                    }
                }
            } else {
                uiState.assistance?.let { assistance ->
                    val status = assistance.status
                    val showProviderToOrigin = status == AssistanceStatus.EN_CAMINO_AL_CLIENTE
                    val showProviderToDestination = status == AssistanceStatus.EN_CAMINO_AL_DESTINO

                    TrackingMap(
                        origin = assistance.origin,
                        destination = assistance.destination,
                        routePositions = if (showProviderToDestination) emptyList() else uiState.assistanceRoutePositions,
                        providerLocation = uiState.providerLocation,
                        providerRoutePositions = if (showProviderToOrigin) uiState.providerToOriginPositions else emptyList(),
                        providerToDestPositions = if (showProviderToDestination) uiState.providerToDestinationPositions else emptyList(),
                        isTracking = isTracking,
                        isProvider = uiState.isProvider
                    )
                }
            }
        }
    }

}

@Composable
private fun PaidSuccessBadge(price: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pago completado - $${String.format(Locale.getDefault(), "%.2f", price)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusBadge(status: AssistanceStatus) {
    val (color, text) = when (status) {
        AssistanceStatus.PENDIENTE -> MaterialTheme.colorScheme.outline to "Pendiente"
        AssistanceStatus.ACEPTADA -> MaterialTheme.colorScheme.primary to "Aceptada"
        AssistanceStatus.EN_CAMINO_AL_CLIENTE -> Color(0xFF4CAF50) to "En camino"
        AssistanceStatus.EN_ORIGEN -> Color(0xFFFF9800) to "En origen"
        AssistanceStatus.EN_CAMINO_AL_DESTINO -> Color(0xFF2196F3) to "Al destino"
        AssistanceStatus.COMPLETADO -> Color(0xFF4CAF50) to "Completado"
        AssistanceStatus.CANCELADO -> MaterialTheme.colorScheme.error to "Cancelado"
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MetricItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProviderActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean,
    isError: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = !isLoading && !isError && enabled,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

