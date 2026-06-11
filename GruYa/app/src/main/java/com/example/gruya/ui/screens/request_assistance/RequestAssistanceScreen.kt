package com.example.gruya.ui.screens.request_assistance

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Vehicle
import com.example.gruya.domain.model.VehicleType
import com.example.gruya.domain.model.displayName
import com.example.gruya.ui.components.VehicleCarouselCard
import com.example.gruya.ui.theme.GruYaTheme
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.UserLocationState
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestAssistanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: RequestAssistanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Location ---
    val locationProvider = rememberFusedLocationProvider()
    val locationState = rememberUserLocationState(locationProvider = locationProvider)

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = -34.6037, longitude = -58.3816),
            zoom = 15.0
        )
    )

    var locationCentered by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // GPS updates ViewModel ONLY on first fix — after that, user controls location
    LaunchedEffect(locationState?.location) {
        if (!locationCentered) {
            locationState?.location?.let { loc ->
                viewModel.onLocationChanged(
                    loc.position.value.latitude,
                    loc.position.value.longitude
                )
            }
        }
    }

    // Animate camera whenever location changes (GPS first fix, map tap, or MyLocation)
    LaunchedEffect(uiState.location) {
        if (locationCentered) {
            uiState.location?.let { (lat, lng) ->
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(lng, lat),
                        zoom = 16.0
                    )
                )
            }
        } else if (uiState.location != null) {
            // First location set — animate and mark as centered
            locationCentered = true
            val loc = uiState.location!!
            cameraState.animateTo(
                CameraPosition(
                    target = Position(loc.second, loc.first),
                    zoom = 16.0
                )
            )
        }
    }

    // MyLocation button: re-center to GPS
    val onCenterToGps: () -> Unit = {
        locationState?.location?.let { loc ->
            viewModel.onLocationChanged(
                loc.position.value.latitude,
                loc.position.value.longitude
            )
        }
    }

    // --- Error Snackbar ---
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // --- Navigate back on success ---
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onNavigateBack()
        }
    }

    RequestAssistanceContent(
        uiState = uiState,
        onVehicleSelected = viewModel::onVehicleSelected,
        onIssueTypeSelected = viewModel::onIssueTypeSelected,
        onSubmit = { viewModel.onSubmit(onSuccess = {}) },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        cameraState = cameraState,
        locationState = locationState,
        onMapClick = { lat, lng -> viewModel.onLocationChanged(lat, lng) },
        onCenterToGps = onCenterToGps
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestAssistanceContent(
    uiState: RequestAssistanceUiState,
    onVehicleSelected: (Int) -> Unit,
    onIssueTypeSelected: (IssueType) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    cameraState: org.maplibre.compose.camera.CameraState? = null,
    locationState: UserLocationState? = null,
    onMapClick: (Double, Double) -> Unit = { _, _ -> },
    onCenterToGps: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Solicitar Auxilio",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // --- Vehicle Carousel ---
            item {
                Text(
                    text = "Seleccioná tu vehículo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                if (uiState.vehicles.isEmpty()) {
                    Text(
                        text = "No tenés vehículos registrados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.vehicles, key = { it.id }) { vehicle ->
                            VehicleCarouselCard(
                                vehicle = vehicle,
                                isSelected = uiState.selectedVehicleId == vehicle.id,
                                onClick = { onVehicleSelected(vehicle.id) }
                            )
                        }
                    }
                }
            }

            // --- Issue Type Grid (2 columns) ---
            item {
                Text(
                    text = "Tipo de problema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IssueType.entries.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { issueType ->
                                val isSelected = uiState.selectedIssueType == issueType

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onIssueTypeSelected(issueType) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = issueTypeIcon(issueType),
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = issueType.displayName,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }

                            // Fill remaining space if odd count
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // --- Location Section ---
            item {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Mini map with selectable location
            item {
                val isDark = isSystemInDarkTheme()

                cameraState?.let { camState ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        MaplibreMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraState = camState,
                            baseStyle = BaseStyle.Uri(
                                if (isDark) DARK_STYLE_URL else LIGHT_STYLE_URL
                            ),
                            options = MapOptions(
                                ornamentOptions = OrnamentOptions(
                                    isCompassEnabled = true,
                                    isScaleBarEnabled = true,
                                    isAttributionEnabled = true,
                                    isLogoEnabled = true
                                )
                            ),
                            onMapClick = { position, _ ->
                                onMapClick(position.latitude, position.longitude)
                                ClickResult.Consume
                            }
                        ) {
                            // Source MUST be created inside MaplibreMap lambda
                            val markerSource = rememberGeoJsonSource(
                                data = GeoJsonData.Features(
                                    geoJson = FeatureCollection(
                                        features = uiState.location?.let { (lat, lng) ->
                                            listOf(
                                                Feature(
                                                    geometry = Point(
                                                        longitude = lng,
                                                        latitude = lat
                                                    ),
                                                    properties = null
                                                )
                                            )
                                        } ?: emptyList()
                                    )
                                )
                            )

                            locationState?.let { state ->
                                LocationPuck(
                                    idPrefix = "user",
                                    location = state.location,
                                    cameraState = camState
                                )
                            }

                            CircleLayer(
                                id = "selected-location",
                                source = markerSource,
                                color = const(MaterialTheme.colorScheme.primary),
                                radius = const(12.dp),
                                strokeColor = const(Color.White),
                                strokeWidth = const(3.dp)
                            )
                        }

                        // MyLocation FAB overlay (bottom-end con padding para no pisar atribución)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 48.dp, end = 8.dp)
                        ) {
                            Button(
                                onClick = onCenterToGps,
                                contentPadding = PaddingValues(8.dp),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "Mi ubicación",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Coordinates text below the map
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (uiState.location != null) {
                            Text(
                                text = "Latitud: ${"%.6f".format(uiState.location.first)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Longitud: ${"%.6f".format(uiState.location.second)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tocá el mapa para cambiar la ubicación",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "Obteniendo ubicación...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // --- Submit Button ---
            item {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(top = 8.dp),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmar Solicitud",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// OpenFreeMap style URLs (matching HomeScreen)
private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

private fun issueTypeIcon(issueType: IssueType): ImageVector = when (issueType) {
    IssueType.NEUMATICO_PINCHADO -> Icons.Outlined.Warning
    IssueType.SIN_COMBUSTIBLE -> Icons.Outlined.LocalGasStation
    IssueType.FALLA_MOTOR -> Icons.Outlined.Engineering
    IssueType.NECESITA_REMOLQUE -> Icons.Outlined.LocalShipping
    IssueType.BATERIA_DESCARGADA -> Icons.Outlined.BatteryChargingFull
    IssueType.LLAVE_OLVIDADA -> Icons.Outlined.VpnKey
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun RequestAssistanceContentPreviewDark() {
    GruYaTheme(darkTheme = true) {
        RequestAssistanceContent(
            uiState = RequestAssistanceUiState(
                vehicles = listOf(
                    Vehicle(1, VehicleType.AUTO, "EX 123 AM", "Ejemplo", "Ejemplo", "Ejemplo", "Blanco"),
                    Vehicle(2, VehicleType.MOTO, "EJ 456 EM", "Ejemplo", "Ejemplo", "Ejemplo", "Rojo")
                ),
                selectedVehicleId = 1,
                selectedIssueType = IssueType.NEUMATICO_PINCHADO,
                location = Pair(-34.6037, -58.3816)
            ),
            onVehicleSelected = {},
            onIssueTypeSelected = {},
            onSubmit = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestAssistanceContentPreviewLight() {
    GruYaTheme(darkTheme = false) {
        RequestAssistanceContent(
            uiState = RequestAssistanceUiState(
                vehicles = listOf(
                    Vehicle(1, VehicleType.AUTO, "EX 123 AM", "Ejemplo", "Ejemplo", "Ejemplo", "Blanco")
                ),
                location = Pair(-34.6037, -58.3816)
            ),
            onVehicleSelected = {},
            onIssueTypeSelected = {},
            onSubmit = {},
            onNavigateBack = {}
        )
    }
}
