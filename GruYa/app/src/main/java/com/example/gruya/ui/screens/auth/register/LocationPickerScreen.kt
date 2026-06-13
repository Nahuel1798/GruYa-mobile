package com.example.gruya.ui.screens.auth.register

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"
private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/bright"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationPickerScreen(
    initialLat: Double?,
    initialLng: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    onBack: () -> Unit
) {
    var selectedLat by remember { mutableStateOf(initialLat) }
    var selectedLng by remember { mutableStateOf(initialLng) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val isDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = if (selectedLat != null && selectedLng != null) {
                Position(selectedLng!!, selectedLat!!)
            } else {
                Position(-58.3816, -34.6037) // Buenos Aires
            },
            zoom = 15.0
        )
    )

    val locationProvider = rememberFusedLocationProvider()
    val userLocationState = if (hasLocationPermission) {
        rememberUserLocationState(locationProvider)
    } else {
        null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Ubicación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedLat != null && selectedLng != null) {
                FloatingActionButton(
                    onClick = { onLocationSelected(selectedLat!!, selectedLng!!) }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = if (isDarkTheme) BaseStyle.Uri(DARK_STYLE_URL) else BaseStyle.Uri(LIGHT_STYLE_URL),
                onMapClick = { position, _ ->
                    selectedLat = position.latitude
                    selectedLng = position.longitude
                    ClickResult.Consume
                }
            ) {
                if (hasLocationPermission) {
                    LocationPuck(
                        idPrefix = "user",
                        location = userLocationState?.location,
                        cameraState = cameraState
                    )
                }

                if (selectedLat != null && selectedLng != null) {
                    val selectedLocationSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            geoJson = FeatureCollection(
                                features = listOf(
                                    Feature(
                                        geometry = Point(
                                            coordinates = Position(selectedLng!!, selectedLat!!)
                                        ),
                                        properties = null
                                    )
                                )
                            )
                        )
                    )

                    CircleLayer(
                        id = "selected-location",
                        source = selectedLocationSource,
                        color = const(Color.Red),
                        radius = const(10.dp),
                        strokeColor = const(Color.White),
                        strokeWidth = const(2.dp)
                    )
                }
            }

            // Botón de mi ubicación
            if (hasLocationPermission) {
                Button(
                    onClick = {
                        userLocationState?.location?.let {
                            val lat = it.position.value.latitude
                            val lng = it.position.value.longitude
                            selectedLat = lat
                            selectedLng = lng
                            scope.launch {
                                cameraState.animateTo(CameraPosition(target = Position(lng, lat), zoom = 15.0))
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                        .size(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }
            }
        }
    }
}
