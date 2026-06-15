package com.example.gruya.ui.screens.request_assistance

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLocation: Pair<Double, Double>?,
    onLocationSelected: (Double, Double) -> Unit,
    onNavigateBack: () -> Unit,
    title: String = "Seleccionar ubicación"
) {
    var selectedLocation by remember { mutableStateOf(initialLocation) }
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = initialLocation?.let { Position(it.second, it.first) }
                ?: Position(-58.3816, -34.6037),
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

    val locationProvider = rememberFusedLocationProvider()
    val locationState = rememberUserLocationState(locationProvider = locationProvider)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
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

                if (hasLocationPermission) {
                    LocationPuck(
                        idPrefix = "user",
                        location = locationState.location,
                        cameraState = cameraState
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

            // Crosshair if no selection yet
            if (selectedLocation == null) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp).align(Alignment.Center)
                )
            }

            // MyLocation FAB
            if (hasLocationPermission) {
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = 70.dp)
                        .size(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
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
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Text("Confirmar ubicación", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
