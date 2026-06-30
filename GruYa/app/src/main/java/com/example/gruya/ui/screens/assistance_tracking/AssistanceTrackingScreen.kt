package com.example.gruya.ui.screens.assistance_tracking

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
import androidx.compose.ui.res.painterResource
import android.util.Log
import com.example.gruya.R
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.TrackingState
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.*
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.json.JSONArray
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
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
                                    onClick = { viewModel.arriveAtOrigin() }
                                )
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
                TrackingMapContent(uiState, isTracking)
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
        enabled = !isLoading && !isError
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

@Composable
private fun TrackingMapContent(uiState: AssistanceTrackingUiState, isTracking: Boolean) {
    val isDarkTheme = isSystemInDarkTheme()
    val assistance = uiState.assistance ?: return
    
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(assistance.origin.longitude, assistance.origin.latitude),
            zoom = 13.0
        )
    )

    // Vista general inicial o cuando no hay seguimiento
    LaunchedEffect(assistance, uiState.providerToOriginRoute) {
        if (!isTracking) {
            val routePositions = assistance.routeGeometry?.let { parseRouteGeometry(it) } ?: emptyList()
            val providerRoutePositions = uiState.providerToOriginRoute?.let { parseRouteGeometry(it) } ?: emptyList()
            
            val points = mutableListOf<Position>()
            points.add(Position(assistance.origin.longitude, assistance.origin.latitude))
            if (assistance.destination.latitude != 0.0) {
                points.add(Position(assistance.destination.longitude, assistance.destination.latitude))
            }
            points.addAll(routePositions)
            points.addAll(providerRoutePositions)

            if (points.isNotEmpty()) {
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLon = points.minOf { it.longitude }
                val maxLon = points.maxOf { it.longitude }

                val target = Position((minLon + maxLon) / 2.0, (minLat + maxLat) / 2.0)
                val deltaLat = maxLat - minLat
                val deltaLon = maxLon - minLon
                
                val zoom = when {
                    deltaLat > 1.0 || deltaLon > 1.0 -> 8.0
                    deltaLat > 0.5 || deltaLon > 0.5 -> 9.0
                    deltaLat > 0.2 || deltaLon > 0.2 -> 10.5
                    deltaLat > 0.1 || deltaLon > 0.1 -> 12.0
                    deltaLat > 0.05 || deltaLon > 0.05 -> 13.0
                    deltaLat > 0.02 || deltaLon > 0.02 -> 14.0
                    else -> 15.0
                }

                cameraState.animateTo(
                    CameraPosition(target = target, zoom = zoom)
                )
            }
        }
    }

    // Auto-zoom y seguimiento a nivel de calle cuando la grúa está en movimiento
    LaunchedEffect(uiState.providerLocation, isTracking) {
        if (isTracking && uiState.providerLocation != null) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(uiState.providerLocation.longitude, uiState.providerLocation.latitude),
                    // Si el zoom es lejano, acercamos a nivel de calle (16.5)
                    // Si ya está cerca, mantenemos el zoom del usuario
                    zoom = if (cameraState.position.zoom < 15.0) 16.5 else cameraState.position.zoom
                )
            )
        }
    }

    MaplibreMap(
        modifier = Modifier.fillMaxSize(),
        cameraState = cameraState,
        baseStyle = if (isDarkTheme) BaseStyle.Uri(DARK_STYLE_URL) else BaseStyle.Uri(LIGHT_STYLE_URL)
    ) {
        val auxilioIcon = image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
        val origenIcon = image(painterResource(R.drawable.ic_origin), drawAsSdf = true)
        val destinoIcon = image(painterResource(R.drawable.ic_destino), drawAsSdf = true)

        // Trace the route if available
        assistance.routeGeometry?.let { geometry ->
            val routePositions = remember(geometry) {
                parseRouteGeometry(geometry)
            }

            if (routePositions.isNotEmpty()) {
                val routeData = remember(routePositions) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(coordinates = routePositions),
                                    properties = null
                                )
                            )
                        )
                    )
                }
                val routeSource = rememberGeoJsonSource(data = routeData)

                // Route casing (border)
                LineLayer(
                    id = "assistance-route-casing",
                    source = routeSource,
                    color = const(Color.White),
                    width = const(9.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round)
                )

                LineLayer(
                    id = "assistance-route",
                    source = routeSource,
                    color = const(MaterialTheme.colorScheme.primary),
                    width = const(6.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round)
                )
            }
        }

        // Trace provider to origin route
        uiState.providerToOriginRoute?.let { geometry ->
            val providerRoutePositions = remember(geometry) {
                parseRouteGeometry(geometry)
            }

            if (providerRoutePositions.isNotEmpty()) {
                val providerRouteData = remember(providerRoutePositions) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(coordinates = providerRoutePositions),
                                    properties = null
                                )
                            )
                        )
                    )
                }
                val providerRouteSource = rememberGeoJsonSource(data = providerRouteData)

                LineLayer(
                    id = "provider-to-origin-route",
                    source = providerRouteSource,
                    color = const(Color(0xFF3B82F6)),
                    width = const(6.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round),
                    dasharray = const(listOf(2f, 2f))
                )
            }
        }

        // Origin and Destination markers
        val markersSource = rememberGeoJsonSource(
            data = remember(assistance) {
                GeoJsonData.Features(
                    geoJson = FeatureCollection(
                        features = listOf(
                            Feature(
                                geometry = Point(Position(assistance.origin.longitude, assistance.origin.latitude)),
                                properties = buildJsonObject { put("type", "origin") }
                            ),
                            Feature(
                                geometry = Point(Position(assistance.destination.longitude, assistance.destination.latitude)),
                                properties = buildJsonObject { put("type", "destination") }
                            )
                        )
                    )
                )
            }
        )

        SymbolLayer(
            id = "markers",
            source = markersSource,
            iconImage = switch(
                input = feature["type"].asString(),
                case("origin", origenIcon),
                case("destination", destinoIcon),
                fallback = origenIcon
            ),
            iconColor = const(MaterialTheme.colorScheme.primary),
            iconSize = const(1.3f),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconAnchor = const(SymbolAnchor.Bottom)
        )

        // Provider marker (The one being tracked) - Show on top
        uiState.providerLocation?.let { location ->
            val providerSource = rememberGeoJsonSource(
                data = remember(location) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = Point(Position(location.longitude, location.latitude)),
                                    properties = buildJsonObject { put("type", "provider") }
                                )
                            )
                        )
                    )
                }
            )

            SymbolLayer(
                id = "provider-marker-layer",
                source = providerSource,
                iconImage = auxilioIcon,
                iconColor = const(
                    if (uiState.isProvider) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.tertiary
                ),
                iconSize = const(1.5f),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                iconAnchor = const(SymbolAnchor.Center)
            )
        }
    }
}

private fun parseRouteGeometry(routeGeometry: String): List<Position> {
    return try {
        val json = JSONArray(routeGeometry)
        buildList {
            for (i in 0 until json.length()) {
                val coord = json.getJSONArray(i)
                add(
                    Position(
                        longitude = coord.getDouble(0),
                        latitude = coord.getDouble(1)
                    )
                )
            }
        }
    } catch (e: org.json.JSONException) {
        Log.w("AssistanceTrackingScreen", "Failed to parse route geometry", e)
        emptyList()
    }
}
