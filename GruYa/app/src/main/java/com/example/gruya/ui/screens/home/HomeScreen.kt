package com.example.gruya.ui.screens.home

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import android.annotation.SuppressLint
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import androidx.compose.material.icons.automirrored.filled.Help


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onLocationPermissionChanged(granted)
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.updateUserLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.updateUserLocation(it.latitude, it.longitude)
                }
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
            val defaultLocation =
                LatLng(-33.2950, -66.3356)

            val cameraPositionState =
                rememberCameraPositionState {
                    position =
                        CameraPosition.fromLatLngZoom(
                            defaultLocation,
                            14f
                        )
                }

            LaunchedEffect(uiState.userLocation) {
                uiState.userLocation?.let {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f)
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = uiState.hasLocationPermission,
                    mapStyleOptions = if (isDarkTheme) {
                        MapStyleOptions(MAP_STYLE_DARK)
                    } else null
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    compassEnabled = true
                )
            ) {
                uiState.userLocation?.let {
                    Marker(
                        state = MarkerState(it),
                        title = "Mi ubicación"
                    )
                }

                // Grúas desde el ViewModel
                uiState.nearbyTowTrucks.forEach { grua ->
                    Marker(
                        state = MarkerState(grua),
                        title = "GruYa"
                    )
                }
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

                                Text("Ver Gruas Cercanas")
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
                                    Icons.Default.Build,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Servicios Mecánicos Cercanos")
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

const val MAP_STYLE_DARK = """
[
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#263c3f"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#6b9a76"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#38414e"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#212a37"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f2835"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#f3d19c"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2f3948"
      }
    ]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#515c6d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  }
]
"""
