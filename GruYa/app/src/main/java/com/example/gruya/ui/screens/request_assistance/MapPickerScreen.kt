package com.example.gruya.ui.screens.request_assistance

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.gruya.ui.components.ScreenScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.example.gruya.R
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.expressions.dsl.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLocation: Pair<Double, Double>?,
    onLocationSelected: (Double, Double) -> Unit,
    onNavigateBack: () -> Unit,
    title: String = "Seleccionar ubicación",
    showNearby: Boolean = false,
    viewModel: MapPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLocation by remember { mutableStateOf(initialLocation) }
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    val locationProvider = rememberFusedLocationProvider()
    val locationState = rememberUserLocationState(locationProvider = locationProvider)
    var hasLoadedNearby by remember { mutableStateOf(false) }

    LaunchedEffect(showNearby, locationState.location, selectedLocation) {
        if (showNearby && !hasLoadedNearby) {
            val location = selectedLocation?.let { Position(it.second, it.first) }
                ?: initialLocation?.let { Position(it.second, it.first) }
                ?: locationState.location?.position?.value

            location?.let {
                viewModel.loadNearbyProviders(it.latitude, it.longitude)
                hasLoadedNearby = true
            }
        }
    }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = initialLocation?.let { Position(it.second, it.first) }
                ?: Position(-66.3356, -33.2950), // San Luis como fallback
            zoom = 15.0
        )
    )

    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(locationState.location) {
        if (initialLocation == null && selectedLocation == null) {
            locationState.location?.let {
                val pos = it.position.value
                cameraState.animateTo(
                    CameraPosition(
                        target = pos,
                        zoom = 15.0
                    )
                )
                selectedLocation = Pair(pos.latitude, pos.longitude)
            }
        }
    }

    ScreenScaffold(
        title = title,
        onBack = onNavigateBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = BaseStyle.Uri(
                    if (isDark) "https://tiles.openfreemap.org/styles/dark" else "https://tiles.openfreemap.org/styles/liberty"
                ),
                onMapClick = { position, _ ->
                    selectedLocation = Pair(position.latitude, position.longitude)
                    ClickResult.Consume
                }
            ) {
                val markerSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = selectedLocation?.let { (lat, lng) ->
                                listOf(
                                    Feature(
                                        geometry = Point(longitude = lng, latitude = lat),
                                        properties = null
                                    )
                                )
                            } ?: emptyList()
                        )
                    )
                )

                val nearbySource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = uiState.nearbyProviders.map { provider ->
                                Feature(
                                    geometry = Point(
                                        longitude = provider.longitude,
                                        latitude = provider.latitude
                                    ),
                                    properties = buildJsonObject {
                                        put("id", provider.id)
                                        put("serviceType", provider.serviceType.uppercase())
                                    }
                                )
                            }
                        )
                    )
                )

                val nearbyStationsSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = uiState.nearbyStations.map { station ->
                                Feature(
                                    geometry = Point(
                                        longitude = station.longitude,
                                        latitude = station.latitude
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

                if (hasLocationPermission) {
                    LocationPuck(
                        idPrefix = "user",
                        location = locationState.location,
                        cameraState = cameraState
                    )
                }

                val auxilioIcon = image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
                val gomeriaIcon = image(painterResource(R.drawable.ic_gomeria), drawAsSdf = true)
                val mecanicoIcon = image(painterResource(R.drawable.ic_mecanico), drawAsSdf = true)
                val fuelStationIcon = image(painterResource(R.drawable.ic_estacionservicio), drawAsSdf = true)

                SymbolLayer(
                    id = "nearby-stations-icons",
                    source = nearbyStationsSource,
                    iconImage = fuelStationIcon,
                    iconColor = const(Color(0xFF4CAF50)),
                    iconSize = const(1.0f),
                    iconAllowOverlap = const(false),
                    iconIgnorePlacement = const(false),
                    onClick = { features ->
                        features.firstOrNull()?.properties?.get("id")?.jsonPrimitive?.longOrNull?.let { id ->
                            uiState.nearbyStations.find { it.id == id }?.let { station ->
                                viewModel.selectStation(station)
                            }
                        }
                        ClickResult.Consume
                    }
                )

                SymbolLayer(
                    id = "nearby-providers-icons",
                    source = nearbySource,
                    iconImage = switch(
                        input = feature["serviceType"].asString(),
                        case("AUXILIO", auxilioIcon),
                        case("GOMERIA", gomeriaIcon),
                        case("MECANICO", mecanicoIcon),
                        fallback = auxilioIcon
                    ),
                    iconColor = switch(
                        input = feature["serviceType"].asString(),
                        case("AUXILIO", const(Color(0xFFFFEB3B))),
                        case("GOMERIA", const(Color(0xFFF4F6F8))),
                        case("MECANICO", const(Color(0xFF3F51B5))),
                        fallback = const(Color.Gray)
                    ),
                    iconSize = const(1.2f),
                    iconAllowOverlap = const(true),
                    iconIgnorePlacement = const(true),
                    onClick = { features ->
                        features.firstOrNull()?.properties?.get("id")?.jsonPrimitive?.intOrNull?.let { id ->
                            uiState.nearbyProviders.find { it.id == id }?.let { provider ->
                                viewModel.selectProvider(provider)
                            }
                        }
                        ClickResult.Consume
                    }
                )

                CircleLayer(
                    id = "selected-location",
                    source = markerSource,
                    color = const(MaterialTheme.colorScheme.primary),
                    radius = const(12.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(3.dp)
                )
            }

            // Crosshair if no selection yet
            if (selectedLocation == null) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp).align(Alignment.Center)
                )
            }

            // FAB Actions
            if (hasLocationPermission) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = 70.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (showNearby) {
                        Button(
                            onClick = {
                                val target = cameraState.position.target
                                viewModel.loadNearbyProviders(target.latitude, target.longitude)
                            },
                            contentPadding = PaddingValues(8.dp),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar área")
                        }
                    }

                    Button(
                        onClick = {
                            locationState.location?.let { loc ->
                                scope.launch {
                                    cameraState.animateTo(
                                        CameraPosition(
                                            target = loc.position.value,
                                            zoom = 16.0
                                        )
                                    )
                                }
                                selectedLocation = Pair(loc.position.value.latitude, loc.position.value.longitude)
                            }
                        },
                        contentPadding = PaddingValues(8.dp),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                    }
                }
            }

            // Confirm Button
            Button(
                onClick = {
                    selectedLocation?.let { (lat, lng) ->
                        onLocationSelected(lat, lng)
                    }
                },
                enabled = selectedLocation != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Confirmar ubicación", fontWeight = FontWeight.SemiBold)
            }

            // Loading Indicator
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        uiState.selectedProvider?.let { provider ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelection() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = provider.companyName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = provider.serviceType.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = provider.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(text = provider.phone, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        uiState.selectedStation?.let { station ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelection() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ESTACIÓN DE SERVICIO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Lat: ${station.latitude}, Lng: ${station.longitude}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
