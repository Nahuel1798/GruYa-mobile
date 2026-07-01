package com.example.gruya.ui.screens.home_provider

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.ChevronRight
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.maplibre.compose.util.ClickResult

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProviderScreen(
    viewModel: HomeProviderViewModel = hiltViewModel(),
    onNavigateToQuote: (Int) -> Unit = {},
    onNavigateToCompleteProfile: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.checkProfileCompletion()
        onPauseOrDispose { }
    }

    LaunchedEffect(uiState.isProfileComplete) {
        if (uiState.isProfileComplete == false) {
            onNavigateToCompleteProfile()
        }
    }

    if (uiState.profileCheckError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "No pudimos verificar tu perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.profileCheckError ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { viewModel.retryProfileCheck() }) {
                    Text("Reintentar")
                }
            }
        }
        return
    }

    if (uiState.isProfileComplete == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    // Location State logic similar to HomeScreen
    val locationProvider = rememberFusedLocationProvider()
    val locationState = if (uiState.hasLocationPermission) {
        rememberUserLocationState(locationProvider = locationProvider)
    } else {
        null
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        viewModel.onLocationPermissionChanged(granted)
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationPermissionLauncher.launch(permissions.toTypedArray())
    }

    var hasLoadedAssistances by remember { mutableStateOf(false) }

    LaunchedEffect(locationState?.location) {
        locationState?.location?.let { location ->
            val lat = location.position.value.latitude
            val lng = location.position.value.longitude

            viewModel.updateUserLocation(
                latitude = lat,
                longitude = lng
            )

            // Reverse Geocoding to get the most accurate address
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()?.let {
                            it.getAddressLine(0) ?: it.locality ?: "Ubicación desconocida"
                        } ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                        viewModel.updateLocationName(address)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val address = addresses?.firstOrNull()?.let {
                        it.getAddressLine(0) ?: it.locality ?: "Ubicación desconocida"
                    } ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                    viewModel.updateLocationName(address)
                }
            } catch (e: Exception) {
                viewModel.updateLocationName("${"%.4f".format(lat)}, ${"%.4f".format(lng)}")
            }

            // Trigger load solo la primera vez que se obtiene ubicación
            if (!hasLoadedAssistances) {
                viewModel.loadNearbyAssistances()
                hasLoadedAssistances = true
            }
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetPeekHeight = 200.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GruYa",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (uiState.isOnline) "En línea" else "Desconectado",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.isOnline) Color.Green else Color.Gray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = uiState.isOnline,
                            onCheckedChange = {
                                viewModel.toggleAvailability()
                            }
                        )
                    }

                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Barra de arrastre (visual)
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Tu ubicación actual",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = uiState.currentLocation,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Solicitudes Cercanas",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.nearbyAssistances.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    "No hay solicitudes pendientes en tu zona",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(uiState.nearbyAssistances) { assistance ->
                        AssistanceRequestCard(
                            assistance = assistance,
                            onClick = { onNavigateToQuote(assistance.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            FullCoverageMap(
                assistances = uiState.nearbyAssistances,
                userLocationState = locationState,
                onRefresh = { viewModel.loadNearbyAssistances() },
                onAssistanceClick = onNavigateToQuote
            )
        }
    }
}
@Composable
fun AssistanceRequestCard(
    assistance: NearbyAssistanceResponse,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = assistance.serviceType.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assistance.clientName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = assistance.vehicle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = assistance.issueType ?: "Sin especificar",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${"%.1f".format(assistance.distanceKm)} km",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FullCoverageMap(
    assistances: List<NearbyAssistanceResponse>,
    userLocationState: org.maplibre.compose.location.UserLocationState? = null,
    onRefresh: () -> Unit = {},
    onAssistanceClick: (Int) -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    var locationCentered by remember { mutableStateOf(false) }
    var selectedAssistance by remember { mutableStateOf<NearbyAssistanceResponse?>(null) }

    val initialPosition = remember(assistances) {
        if (assistances.isNotEmpty()) {
            Position(assistances[0].origin.longitude, assistances[0].origin.latitude)
        } else {
            Position(-66.3356, -33.2950)
        }
    }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = initialPosition,
            zoom = 11.0
        )
    )

    // Center on user location once
    LaunchedEffect(userLocationState?.location) {
        if (!locationCentered) {
            userLocationState?.location?.let { location ->
                cameraState.animateTo(
                    CameraPosition(
                        target = location.position.value,
                        zoom = 12.0
                    )
                )
                locationCentered = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            baseStyle = if (isDarkTheme) {
                BaseStyle.Uri(DARK_STYLE_URL)
            } else {
                BaseStyle.Uri(LIGHT_STYLE_URL)
            },
            onMapClick = { _, _ ->
                selectedAssistance = null
                ClickResult.Pass
            }
        ) {
            userLocationState?.let {
                LocationTrackingEffect(locationState = it, onLocationChange = {})
                LocationPuck(
                    idPrefix = "provider-location",
                    location = it.location,
                    cameraState = cameraState
                )
            }

            val assistanceSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(
                    geoJson = FeatureCollection(
                        features = assistances.map { assistance ->
                            Feature(
                                geometry = Point(
                                    coordinates = Position(
                                        longitude = assistance.origin.longitude,
                                        latitude = assistance.origin.latitude
                                    )
                                ),
                                properties = buildJsonObject {
                                    put("id", assistance.id)
                                    put("type", assistance.serviceType)
                                }
                            )
                        }
                    )
                )
            )

            CircleLayer(
                id = "assistance-markers",
                source = assistanceSource,
                color = const(MaterialTheme.colorScheme.primary),
                radius = const(10.dp),
                strokeColor = const(Color.White),
                strokeWidth = const(2.dp),
                onClick = { features ->
                    features.firstOrNull()?.properties?.get("id")?.jsonPrimitive?.intOrNull?.let { id ->
                        selectedAssistance = assistances.find { it.id == id }
                    }
                    ClickResult.Consume
                }
            )
        }

        // Overlay Button for Selected Assistance
        selectedAssistance?.let { assistance ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 220.dp) // Above the bottom sheet peek
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = assistance.clientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${assistance.serviceType} • ${"%.1f".format(assistance.distanceKm)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onAssistanceClick(assistance.id) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Revisar")
                    }
                }
            }
        }

        // Botón de refrescar manual
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar solicitudes",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
