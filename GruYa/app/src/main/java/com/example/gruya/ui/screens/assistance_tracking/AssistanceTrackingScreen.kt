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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistanceTrackingScreen(
    assistanceId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AssistanceTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assistanceId) {
        viewModel.loadAssistance(assistanceId)
    }

    Scaffold(
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
                    Button(onClick = { viewModel.loadAssistance(assistanceId) }) {
                        Text("Reintentar")
                    }
                }
            } else {
                TrackingMapContent(uiState)
                
                // Bottom Info Card
                uiState.assistance?.let { assistance ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
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
                            
                            Button(
                                onClick = { /* TODO: Call client */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Llamar Cliente")
                            }
                        }
                    }
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
private fun TrackingMapContent(uiState: AssistanceTrackingUiState) {
    val isDarkTheme = isSystemInDarkTheme()
    val assistance = uiState.assistance ?: return
    
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(assistance.origin.longitude, assistance.origin.latitude),
            zoom = 13.0
        )
    )

    MaplibreMap(
        modifier = Modifier.fillMaxSize(),
        cameraState = cameraState,
        baseStyle = if (isDarkTheme) BaseStyle.Uri(DARK_STYLE_URL) else BaseStyle.Uri(LIGHT_STYLE_URL)
    ) {
        val markersSource = rememberGeoJsonSource(
            data = GeoJsonData.Features(
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
        )

        CircleLayer(
            id = "markers",
            source = markersSource,
            color = const(MaterialTheme.colorScheme.primary),
            radius = const(10.dp),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp)
        )
    }
}
