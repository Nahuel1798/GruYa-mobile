package com.example.gruya.ui.screens.quote

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gruya.ui.components.AppTextField
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point

import androidx.compose.ui.tooling.preview.Preview
import com.example.gruya.ui.theme.GruYaTheme
import com.example.gruya.domain.model.displayName
import com.example.gruya.domain.model.*
import com.example.gruya.data.remote.dtos.response.*

import android.location.Geocoder
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

import org.json.JSONArray
import org.maplibre.spatialk.geojson.Position

fun parseRouteGeometry(routeGeometry: String): List<Position> {
    val json = JSONArray(routeGeometry)

    return buildList {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    assistanceId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: QuoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    // Initialize ID in ViewModel
    LaunchedEffect(assistanceId) {
        assistanceId?.let { viewModel.setAssistanceId(it) }
    }
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    // Geocoding effect
    LaunchedEffect(uiState.assistanceRequest) {
        val assistance = uiState.assistanceRequest ?: return@LaunchedEffect
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(assistance.origin.latitude, assistance.origin.longitude, 1) { addresses ->
                    val addr = addresses.firstOrNull()?.getAddressLine(0)
                    viewModel.updateAddresses(origin = addr, destination = null)
                }
                geocoder.getFromLocation(assistance.destination.latitude, assistance.destination.longitude, 1) { addresses ->
                    val addr = addresses.firstOrNull()?.getAddressLine(0)
                    viewModel.updateAddresses(origin = null, destination = addr)
                }
            } else {
                @Suppress("DEPRECATION")
                val originAddr = geocoder.getFromLocation(assistance.origin.latitude, assistance.origin.longitude, 1)
                    ?.firstOrNull()?.getAddressLine(0)
                @Suppress("DEPRECATION")
                val destAddr = geocoder.getFromLocation(assistance.destination.latitude, assistance.destination.longitude, 1)
                    ?.firstOrNull()?.getAddressLine(0)
                
                viewModel.updateAddresses(origin = originAddr, destination = destAddr)
            }
        } catch (e: Exception) {
            // Silently fail geocoding
        }
    }

    QuoteContent(
        uiState = uiState,
        isExpanded = isExpanded,
        onExpandedToggle = { isExpanded = !isExpanded },
        onPriceChanged = viewModel::onPriceChanged,
        onSubmitQuote = viewModel::submitQuote,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteContent(
    uiState: QuoteUiState,
    isExpanded: Boolean,
    onExpandedToggle: () -> Unit,
    onPriceChanged: (String) -> Unit,
    onSubmitQuote: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotizar Servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Map
            val cameraState = rememberCameraState(
                firstPosition = CameraPosition(
                    target = Position(-58.3816, -34.6037),
                    zoom = 13.0
                )
            )

            // Update camera when assistance changes
            LaunchedEffect(uiState.assistanceRequest) {
                uiState.assistanceRequest?.let { assistance ->
                    val target = if (assistance.destination.latitude != 0.0) {
                        Position(
                            (assistance.origin.longitude + assistance.destination.longitude) / 2.0,
                            (assistance.origin.latitude + assistance.destination.latitude) / 2.0
                        )
                    } else {
                        Position(assistance.origin.longitude, assistance.origin.latitude)
                    }
                    
                    cameraState.animateTo(
                        CameraPosition(
                            target = target,
                            zoom = 11.5 // Zoom reducido para ver la ciudad
                        )
                    )
                }
            }

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = BaseStyle.Uri(
                    if (isDark) "https://tiles.openfreemap.org/styles/dark" else "https://tiles.openfreemap.org/styles/liberty"
                )
            ) {
                uiState.assistanceRequest?.let { assistance ->
                    val features = mutableListOf(
                        Feature(
                            geometry = Point(
                                longitude = assistance.origin.longitude,
                                latitude = assistance.origin.latitude
                            ),
                            properties = null
                        )
                    )
                    
                    // Agregar destino si es diferente al origen
                    if (assistance.destination.latitude != 0.0) {
                        features.add(
                            Feature(
                                geometry = Point(
                                    longitude = assistance.destination.longitude,
                                    latitude = assistance.destination.latitude
                                ),
                                properties = null
                            )
                        )
                    }

                    // Trazar la ruta (línea recta entre origen y destino)
                    assistance.routeGeometry?.let { geometry ->

                        val routePositions = remember(geometry) {
                            parseRouteGeometry(geometry)
                        }

                        if (routePositions.isNotEmpty()) {

                            val routeSource = rememberGeoJsonSource(
                                data = GeoJsonData.Features(
                                    geoJson = FeatureCollection(
                                        features = listOf(
                                            Feature(
                                                geometry = LineString(
                                                    coordinates = routePositions
                                                ),
                                                properties = null
                                            )
                                        )
                                    )
                                )
                            )

                            LineLayer(
                                id = "assistance-route",
                                source = routeSource,
                                color = const(MaterialTheme.colorScheme.primary),
                                width = const(6.dp)
                            )
                        }
                    }

                    val markersSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            geoJson = FeatureCollection(features = features)
                        )
                    )
                    
                    CircleLayer(
                        id = "assistance-points",
                        source = markersSource,
                        color = const(MaterialTheme.colorScheme.primary),
                        radius = const(10.dp),
                        strokeColor = const(Color.White),
                        strokeWidth = const(2.dp)
                    )
                }
            }

            // Bottom Panel
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .then(if (isExpanded) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isLoading && uiState.assistanceRequest == null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (uiState.assistanceRequest != null) {
                        val assistance = uiState.assistanceRequest!!

                        // Header (Clickable to expand/collapse)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExpandedToggle() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${assistance.client.firstName} ${assistance.client.lastName}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${assistance.vehicle.brand} ${assistance.vehicle.model}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = assistance.issueType.displayName,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        
                        if (isExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailItem(
                                    icon = Icons.Outlined.DirectionsCar,
                                    label = "Vehículo",
                                    value = "${assistance.vehicle.brand} ${assistance.vehicle.model} (${assistance.vehicle.licensePlate})"
                                )
                                uiState.originAddress?.let {
                                    DetailItem(
                                        icon = Icons.Outlined.LocationOn,
                                        label = "Origen",
                                        value = it
                                    )
                                }
                                uiState.destinationAddress?.let {
                                    DetailItem(
                                        icon = Icons.Outlined.Flag,
                                        label = "Destino",
                                        value = it
                                    )
                                }
                                DetailItem(
                                    icon = Icons.Outlined.Route,
                                    label = "Distancia",
                                    value = "${"%.1f".format(assistance.distanceKm ?: 0.0)} km"
                                )
                                // ETA Estimado
                                val eta = assistance.etaMinutes?.toInt() ?: (assistance.distanceKm?.let { (it * 3).toInt() } ?: 5).coerceAtLeast(5)
                                DetailItem(
                                    icon = Icons.Outlined.AccessTime,
                                    label = "Llegada estimada",
                                    value = "~$eta min"
                                )
                                DetailItem(
                                    icon = Icons.Outlined.Build,
                                    label = "Servicio",
                                    value = assistance.serviceType.displayName
                                )
                                DetailItem(
                                    icon = Icons.Outlined.Info,
                                    label = "Problema",
                                    value = assistance.issueType.displayName
                                )
                            }

                            AppTextField(
                                value = uiState.price,
                                onValueChange = onPriceChanged,
                                placeholder = "Precio del servicio",
                                leadingIcon = Icons.Default.AttachMoney,
                                keyboardType = KeyboardType.Number,
                                errorMessage = uiState.error
                            )

                            Button(
                                onClick = onSubmitQuote,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = uiState.price.isNotBlank() && !uiState.isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                } else {
                                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enviar Presupuesto", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    } else if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "Ocurrió un error inesperado",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (uiState.isSubmitted) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Entendido")
                }
            },
            title = { Text("¡Presupuesto Enviado!") },
            text = { Text("Tu propuesta ha sido enviada al cliente. Te avisaremos si es aceptada para comenzar el servicio.") },
            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50)) }
        )
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteScreenPreview() {
    GruYaTheme {
        val mockAssistance = AssistanceResponse(
            id = 1,
            serviceType = ServiceType.AUXILIO,
            issueType = IssueType.NEUMATICO_PINCHADO,
            status = AssistanceStatus.PENDIENTE,
            vehicle = VehicleResponse(
                id = 1,
                type = VehicleType.AUTO,
                licensePlate = "ABC-123",
                brand = "Toyota",
                model = "Corolla",
                insurance = "La Caja",
                color = "Blanco"
            ),
            providerProfile = null,
            client = UserResponse(
                id = 1,
                firstName = "Juan",
                lastName = "Pérez",
                email = "juan@example.com",
                role = null,
                avatarUrl = null,
                phone = "12345678"
            ),
            distanceKm = 5.4,
            etaMinutes = 15.0,
            routeGeometry = null,
            origin = Location(-34.6037, -58.3816),
            destination = Location(-34.6137, -58.3916),
            isDirected = false
        )

        QuoteContent(
            uiState = QuoteUiState(
                assistanceRequest = mockAssistance,
                isLoading = false,
                price = "15000",
                originAddress = "Av. 9 de Julio, CABA",
                destinationAddress = "Congreso, CABA"
            ),
            isExpanded = true,
            onExpandedToggle = {},
            onPriceChanged = {},
            onSubmitQuote = {},
            onNavigateBack = {}
        )
    }
}
