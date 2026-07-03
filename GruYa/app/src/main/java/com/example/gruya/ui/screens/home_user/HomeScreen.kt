package com.example.gruya.ui.screens.home_user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.annotation.SuppressLint
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.example.gruya.R
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.ui.theme.Success
import com.example.gruya.ui.theme.Warning
import com.example.gruya.ui.theme.Info
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.expressions.dsl.*
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.util.ClickResult
import kotlinx.serialization.json.*
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRequestAssistance: (Int?, String?, Double?, Double?) -> Unit = { _, _, _, _ -> },
    onNavigateToNotifications: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error de estaciones si existe
    LaunchedEffect(uiState.stationsError) {
        uiState.stationsError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    // 1. Map & Location State
    val locationProvider = rememberFusedLocationProvider()
    val locationState = if (uiState.hasLocationPermission) {
        rememberUserLocationState(locationProvider = locationProvider)
    } else {
        null
    }

    val defaultLocation = Position(-66.3356, -33.2950)
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = defaultLocation,
            zoom = 12.0
        )
    )

    var locationCentered by remember { mutableStateOf(false) }

    // 2. Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        viewModel.onLocationPermissionChanged(granted)
    }

    // 3. Request permissions on launch
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // 4. Update ViewModel and Initial Camera Center
    LaunchedEffect(locationState?.location) {
        locationState?.location?.let { location ->
            val lat = location.position.value.latitude
            val lng = location.position.value.longitude

            viewModel.updateUserLocation(
                latitud = lat,
                longitud = lng
            )

            if (!locationCentered) {
                cameraState.animateTo(
                    CameraPosition(
                        target = location.position.value,
                        zoom = 14.0
                    )
                )

                locationCentered = true

                // Cargar grúas cercanas una sola vez con la ubicación obtenida
                viewModel.loadService(lat, lng)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isMapFullScreen,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = {
                        androidx.compose.material3.Text(
                            "GruYa",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (uiState.isMapFullScreen) {
                FloatingActionButton(
                    onClick = viewModel::toggleMapFullScreen,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar Mapa Completo")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // MAPA (Fondo)
            val isDarkTheme = isSystemInDarkTheme()

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = if (isDarkTheme) {
                    BaseStyle.Uri(DARK_STYLE_URL)
                } else {
                    BaseStyle.Uri(LIGHT_STYLE_URL)
                },
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        isCompassEnabled = false,
                        isScaleBarEnabled = false,
                        isAttributionEnabled = false,
                        isLogoEnabled = false
                    )
                )
            ) {
                // Tow truck GeoJSON source
                val towTruckSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = uiState.nearbyTowTrucks.map { provider ->
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(
                                            longitude = provider.longitude,
                                            latitude = provider.latitude
                                        )
                                    ),
                                    properties = buildJsonObject {
                                        put("id", provider.id)
                                        put("name", provider.companyName)
                                        put("serviceType", provider.serviceType.uppercase())
                                    }
                                )
                            }
                        )
                    )
                )

                // Fuel station GeoJSON source
                val fuelStationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = uiState.nearbyFuelStations.map { station ->
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(
                                            longitude = station.longitude,
                                            latitude = station.latitude
                                        )
                                    ),
                                    properties = buildJsonObject {
                                        put("id", station.id)
                                        put("name", station.name)
                                    }
                                )
                            }
                        )
                    )
                )

                locationState?.let {
                    LocationTrackingEffect(locationState = it, onLocationChange = {})
                    LocationPuck(
                        idPrefix = "user-location",
                        location = it.location,
                        cameraState = cameraState
                    )
                }

                val auxilioIcon = image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
                val gomeriaIcon = image(painterResource(R.drawable.ic_gomeria), drawAsSdf = true)
                val mecanicoIcon = image(painterResource(R.drawable.ic_mecanico), drawAsSdf = true)
                val fuelStationIcon = image(painterResource(R.drawable.ic_estacionservicio), drawAsSdf = true)

                // Tow truck icons
                SymbolLayer(
                    id = "tow-trucks-icons",
                    source = towTruckSource,
                    iconImage = switch(
                        input = feature["serviceType"].asString(),
                        case("AUXILIO", auxilioIcon),
                        case("GOMERIA", gomeriaIcon),
                        case("MECANICO", mecanicoIcon),
                        fallback = auxilioIcon
                    ),
                    iconColor = switch(
                        input = feature["serviceType"].asString(),
                        case("AUXILIO", const(Color(0xFFFFD600))),
                        case("GOMERIA", const(Color(0xFF424242))),
                        case("MECANICO", const(Color(0xFF1976D2))),
                        fallback = const(Color.Gray)
                    ),
                    iconSize = const(1.3f),
                    iconAllowOverlap = const(true),
                    iconIgnorePlacement = const(true),
                    onClick = { features ->
                        val clickedFeature = features.firstOrNull()
                        val id = clickedFeature?.properties?.get("id")?.jsonPrimitive?.intOrNull
                        id?.let { clickedId ->
                            uiState.nearbyTowTrucks.find { it.id == clickedId }?.let { provider ->
                                viewModel.selectProvider(provider)
                            }
                        }
                        ClickResult.Consume
                    }
                )

                // Fuel station icons
                SymbolLayer(
                    id = "fuel-stations-icons",
                    source = fuelStationSource,
                    iconImage = fuelStationIcon,
                    iconColor = const(Color(0xFF4CAF50)),
                    iconSize = const(1.0f),
                    iconAllowOverlap = const(false),
                    iconIgnorePlacement = const(false)
                )
            }

            // Loading Indicator
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = padding.calculateTopPadding())
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Map Actions Overlay (Refresh & Location)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = padding.calculateTopPadding() + 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { viewModel.loadService() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        locationState?.location?.let { location ->
                            scope.launch {
                                cameraState.animateTo(
                                    CameraPosition(
                                        target = location.position.value,
                                        zoom = 16.0
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Panel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(
                    visible = uiState.panelVisible && !uiState.isMapFullScreen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .animateContentSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(50)
                                    )
                                    .align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            androidx.compose.material3.Text(
                                text = "¿Qué necesitas hoy?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                androidx.compose.material3.Text(
                                    text = "${uiState.nearbyTowTrucks.size} servicios disponibles cerca",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    onNavigateToRequestAssistance(
                                        null,
                                        null,
                                        uiState.userLocation?.latitude,
                                        uiState.userLocation?.longitude
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                androidx.compose.material3.Text(
                                    text = "SOLICITAR AUXILIO AHORA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // Selected Provider Sheet
            uiState.selectedProvider?.let { provider ->
                ProviderDetailSheet(
                    provider = provider,
                    context = context,
                    onDismiss = viewModel::clearSelectedProvider,
                    onNavigateToRequest = {
                        viewModel.clearSelectedProvider()
                        onNavigateToRequestAssistance(
                            provider.id,
                            provider.serviceType,
                            uiState.userLocation?.latitude,
                            uiState.userLocation?.longitude
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderDetailSheet(
    provider: ProviderLocationResponse,
    context: android.content.Context,
    onDismiss: () -> Unit,
    onNavigateToRequest: () -> Unit
) {
    var addressText by remember(provider.id) { mutableStateOf("Cargando ubicación...") }

    LaunchedEffect(provider.id) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(provider.latitude, provider.longitude, 1)
            }
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare ?: ""
                val number = address.subThoroughfare ?: ""
                val city = address.locality ?: ""
                addressText = if (street.isNotEmpty()) "$street $number, $city" else city
            } else {
                addressText = "Ubicación no disponible"
            }
        } catch (_: Exception) {
            addressText = "Lat: ${provider.latitude}, Lng: ${provider.longitude}"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = provider.companyName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(provider.serviceType.uppercase()) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                val statusColor = if (provider.isAvailable) Success else MaterialTheme.colorScheme.error
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (provider.isAvailable) "DISPONIBLE" else "OCUPADO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Información del servicio",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = provider.description.ifEmpty { "Sin descripción disponible." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            InfoRow(
                icon = Icons.Default.Phone,
                label = "Contacto",
                value = provider.phone
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Ubicación aproximada",
                value = addressText
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(56.dp),
                    onClick = {
                        val url = "https://wa.me/${provider.phone.filter { it.isDigit() }}"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color(0xFF25D366))
                    Spacer(Modifier.width(8.dp))
                    Text("WhatsApp", color = Color(0xFF25D366))
                }

                Button(
                    modifier = Modifier.weight(1f).height(56.dp),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:${provider.phone}".toUri())
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar")
                }
            }

            if (provider.serviceType.equals("AUXILIO", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = onNavigateToRequest,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        "SOLICITAR ASISTENCIA",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"
