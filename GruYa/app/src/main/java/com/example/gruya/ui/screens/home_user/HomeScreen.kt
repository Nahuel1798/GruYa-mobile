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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import dagger.hilt.android.lifecycle.HiltViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

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
            zoom = 14.0
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

            viewModel.updateUserLocation(
                latitud = location.position.value.latitude,
                longitud = location.position.value.longitude
            )

            if (!locationCentered) {
                cameraState.animateTo(
                    CameraPosition(
                        target = location.position.value,
                        zoom = 16.0
                    )
                )

                locationCentered = true

                // Cargar grúas cercanas una sola vez
                viewModel.loadService()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isMapFullScreen,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "GruYa",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            if (uiState.isMapFullScreen) {
                FloatingActionButton(
                    onClick = viewModel::toggleMapFullScreen,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar Mapa Completo")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                        isCompassEnabled = true,
                        isScaleBarEnabled = true,
                        isAttributionEnabled = true,
                        isLogoEnabled = true
                    )
                )
            ) {
                // Tow truck GeoJSON source
                val towTruckSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = uiState.nearbyTowTrucks.map { pos ->
                                Feature(
                                    geometry = Point(
                                        longitude = pos.longitude,
                                        latitude = pos.latitude
                                    ),
                                    properties = null
                                )
                            }
                        )
                    )
                )

                locationState?.let { state ->
                    LocationPuck(
                        idPrefix = "user",
                        location = state.location,
                        cameraState = cameraState
                    )

                    LocationTrackingEffect(
                        locationState = state,
                        enabled = uiState.hasLocationPermission
                    ) {
                        // The camera centering is handled by the LaunchedEffect initially
                        // and by the MyLocation FAB. We can also add logic here to follow
                        // the user if a "follow mode" is active.
                    }
                }

                // Tow truck circles
                CircleLayer(
                    id = "tow-trucks",
                    source = towTruckSource,
                    color = const(Color(0xFFFF6D00)),
                    radius = const(8.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.dp)
                )
            }

            // My Location FAB (Overlay)
            FloatingActionButton(
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
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Padding del TopAppBar
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                // SEARCH BAR
                AnimatedVisibility(
                    visible = !uiState.isMapFullScreen,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = uiState.searchText,
                                onValueChange = viewModel::onSearchChange,
                                placeholder = {
                                    Text("¿A dónde necesitas ayuda?")
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Mapa",
                                modifier = Modifier.clickable { viewModel.toggleMapFullScreen() },
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(
                    visible = uiState.panelVisible && !uiState.isMapFullScreen,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight }
                    ) + fadeOut()
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 10.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(5.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(50)
                                    )
                                    .align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "¿Qué necesitas hoy?",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${uiState.nearbyTowTrucks.size} grúas disponibles cerca de ti",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Selecciona un servicio para recibir asistencia inmediata.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = viewModel::showRequestDialog,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Solicitar Auxilio",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddLocation,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Ver Servicios Cercanos")
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }

            // DIALOGO
            if (uiState.showDialog) {
                AlertDialog(
                    onDismissRequest = viewModel::hideRequestDialog,
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.requestTowTruck()
                                viewModel.hideRequestDialog()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Confirmar y Pedir Grúa")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = viewModel::hideRequestDialog
                        ) {
                            Text("Cancelar")
                        }
                    },
                    title = {
                        Text(
                            "Confirmar Auxilio",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            "Estamos por enviar una unidad de emergencia a tu ubicación actual.",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}

// OpenFreeMap style URLs
private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"
