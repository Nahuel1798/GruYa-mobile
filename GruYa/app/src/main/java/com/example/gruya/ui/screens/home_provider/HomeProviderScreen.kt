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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProviderScreen(
    viewModel: HomeProviderViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(locationState?.location) {
        locationState?.location?.let { location ->
            val lat = location.position.value.latitude
            val lng = location.position.value.longitude

            viewModel.updateUserLocation(
                latitude = lat,
                longitude = lng
            )

            // Reverse Geocoding to get address name
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()?.let {
                            it.locality ?: it.subAdminArea ?: it.adminArea ?: "Ubicación desconocida"
                        } ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                        viewModel.updateLocationName(address)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val address = addresses?.firstOrNull()?.let {
                        it.locality ?: it.subAdminArea ?: it.adminArea ?: "Ubicación desconocida"
                    } ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                    viewModel.updateLocationName(address)
                }
            } catch (e: Exception) {
                viewModel.updateLocationName("${"%.4f".format(lat)}, ${"%.4f".format(lng)}")
            }

            // Trigger load when location is first obtained
            viewModel.loadNearbyAssistances()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GruYa",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {

                    Switch(
                        checked = uiState.isOnline,
                        onCheckedChange = {
                            viewModel.toggleAvailability()
                        }
                    )

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Text(
                    text = "Panel de Operador",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusBadge(uiState.isOnline)
            }

            item {

                StatsSection(
                    services = uiState.todayServices,
                    earnings = uiState.earnings,
                    location = uiState.currentLocation
                )
            }

            item {
                Text(
                    text = "Mapa de Solicitudes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                CoverageMapCard(
                    assistances = uiState.nearbyAssistances,
                    userLocationState = locationState
                )
            }

            item {
                Text(
                    text = "Solicitudes Cercanas",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.nearbyAssistances) { assistance ->
                AssistanceRequestCard(assistance)
            }
        }
    }
}
@Composable
fun StatusBadge(
    isOnline: Boolean
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (isOnline)
                            Color.Green
                        else
                            Color.Red,
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text =
                    if (isOnline)
                        "Disponible"
                    else
                        "Desconectado",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatsSection(
    services: Int,
    earnings: Double,
    location: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatCard(
                title = "SERVICIOS HOY",
                value = services.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "GANANCIAS",
                value = "$$earnings",
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    location,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
fun CoverageMapCard(
    assistances: List<NearbyAssistanceResponse>,
    userLocationState: org.maplibre.compose.location.UserLocationState? = null
) {
    val isDarkTheme = isSystemInDarkTheme()
    var locationCentered by remember { mutableStateOf(false) }

    val initialPosition = remember(assistances) {
        if (assistances.isNotEmpty()) {
            Position(assistances[0].longitude, assistances[0].latitude)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = if (isDarkTheme) {
                    BaseStyle.Uri(DARK_STYLE_URL)
                } else {
                    BaseStyle.Uri(LIGHT_STYLE_URL)
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
                                            longitude = assistance.longitude,
                                            latitude = assistance.latitude
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
                    radius = const(8.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.dp)
                )
            }
            
            if (assistances.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay solicitudes cercanas", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
