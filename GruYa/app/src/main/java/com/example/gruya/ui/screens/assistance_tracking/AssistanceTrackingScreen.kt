package com.example.gruya.ui.screens.assistance_tracking

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.gruya.domain.model.TrackingState
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Check

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistanceTrackingScreen(
    assistanceId: Int,
    trackingSessionId: String? = null,
    onNavigateBack: () -> Unit,
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
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        sheetPeekHeight = 140.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetShadowElevation = 8.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            uiState.assistance?.let { assistance ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
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
                                text = "${assistance.client.firstName} ${assistance.client.lastName}",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(Icons.Default.LocationOn, "Destino", uiState.destinationAddress ?: "Cargando...")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Provider-specific controls
                    if (uiState.isProvider) {
                        val trackingState = uiState.trackingState
                        val status = assistance.status

                        if (trackingState is TrackingState.Error) {
                            Text(
                                text = "Error: ${trackingState.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        when (status) {
                            AssistanceStatus.ACEPTADA -> {
                                ProviderActionButton(
                                    label = "Iniciar viaje",
                                    icon = Icons.Default.PlayArrow,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    onClick = { viewModel.startTrip() }
                                )
                            }
                            AssistanceStatus.EN_CAMINO_AL_CLIENTE -> {
                                ProviderActionButton(
                                    label = "Llegué al cliente",
                                    icon = Icons.Default.LocationOn,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    enabled = uiState.isNearOrigin,
                                    onClick = { viewModel.arriveAtOrigin() }
                                )
                                if (!uiState.isNearOrigin && !uiState.isLoading) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Debes estar a menos de 300m del origen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            AssistanceStatus.EN_ORIGEN -> {
                                ProviderActionButton(
                                    label = "Ir al destino",
                                    icon = Icons.Default.Flag,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    onClick = { viewModel.headToDestination() }
                                )
                            }
                            AssistanceStatus.EN_CAMINO_AL_DESTINO -> {
                                ProviderActionButton(
                                    label = "Finalizar servicio",
                                    icon = Icons.Default.Check,
                                    isLoading = uiState.isLoading,
                                    isError = trackingState is TrackingState.Error,
                                    onClick = { viewModel.completeService() }
                                )
                            }
                            AssistanceStatus.COMPLETADO -> {
                                Text(
                                    text = "Servicio completado",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            AssistanceStatus.CANCELADO -> {
                                Text(
                                    text = "Servicio cancelado",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            AssistanceStatus.PENDIENTE -> {
                                Text(
                                    text = "Esperando asignación...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    else {
                        // Client view: show status
                        when (val trackingState = uiState.trackingState) {
                            is TrackingState.Connected, is TrackingState.Tracking -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Proveedor en camino",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (assistance.distanceKm != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "%.1f km · ~%.0f min".format(
                                            assistance.distanceKm,
                                            assistance.etaMinutes ?: 0.0
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            is TrackingState.Disconnected -> {
                                Text(
                                    text = "Proveedor desconectado",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            is TrackingState.Error -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Error de conexión",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Text(
                                        text = trackingState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            uiState.assistance?.trackingSessionId?.let { sessionId ->
                                                viewModel.loadAssistance(assistanceId, sessionId)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Text("Reintentar conexión")
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = "Esperando conexión...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } ?: Box(Modifier.fillMaxWidth().height(100.dp))
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadAssistance(assistanceId, trackingSessionId) }) {
                        Text("Reintentar")
                    }
                }
            } else {
                uiState.assistance?.let { assistance ->
                    val status = assistance.status
                    val showProviderToOrigin = status == AssistanceStatus.ACEPTADA ||
                            status == AssistanceStatus.EN_CAMINO_AL_CLIENTE

                    TrackingMap(
                        origin = assistance.origin,
                        destination = assistance.destination,
                        routeGeometry = assistance.routeGeometry,
                        providerLocation = uiState.providerLocation,
                        providerToOriginRoute = if (showProviderToOrigin) uiState.providerToOriginRoute else null,
                        isTracking = isTracking,
                        isProvider = uiState.isProvider
                    )
                }
            }
        }
    }
}


@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = !isLoading && !isError && enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}
