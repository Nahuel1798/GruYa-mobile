package com.example.gruya.ui.screens.auth.register

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gruya.domain.model.ServiceType
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
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

private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"
private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/bright"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    uiState: ProviderProfileUiState,
    onBack: () -> Unit,
    onServiceTypeChange: (ServiceType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
    onAddressChange: (String) -> Unit,
    onLocationChange: (Double, Double) -> Unit,
    onConfirm: () -> Unit
) {
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Prestador",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            HeroSection()

            Text(
                text = "Tipo de Servicio",
                style = MaterialTheme.typography.labelLarge
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    ServiceCard(
                        title = "Grúas",
                        icon = Icons.Default.LocalShipping,
                        selected = uiState.serviceType == ServiceType.AUXILIO
                    ) {
                        onServiceTypeChange(ServiceType.AUXILIO)
                    }
                }

                item {

                    ServiceCard(
                        title = "Mecánico",
                        icon = Icons.Default.Build,
                        selected = uiState.serviceType == ServiceType.MECANICO
                    ) {
                        onServiceTypeChange(ServiceType.MECANICO)
                    }
                }

                item {

                    ServiceCard(
                        title = "Gomería",
                        icon = Icons.Default.TireRepair,
                        selected = uiState.serviceType == ServiceType.GOMERIA
                    ) {
                        onServiceTypeChange(ServiceType.GOMERIA)
                    }
                }
            }

            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = {
                    Text("Descripción")
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Disponible",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Recibir solicitudes ahora"
                        )
                    }

                    Switch(
                        checked = uiState.available,
                        onCheckedChange = onAvailableChange
                    )
                }
            }

            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.labelLarge
            )

            InteractiveMap(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                hasPermission = hasLocationPermission,
                onLocationSelected = onLocationChange
            )

            OutlinedTextField(
                value = uiState.address,
                onValueChange = onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null
                    )
                },
                label = {
                    Text("Dirección")
                }
            )

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onConfirm,
                enabled = !uiState.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                if (uiState.loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Completar Perfil")
                }
            }
        }
    }
}

@Composable
fun HeroSection() {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "PASO FINAL",
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configure su perfil profesional",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Indique qué servicios ofrece y dónde opera."
            )
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(title)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun InteractiveMap(
    latitude: Double?,
    longitude: Double?,
    hasPermission: Boolean,
    onLocationSelected: (Double, Double) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = if (latitude != null && longitude != null) {
                Position(longitude, latitude)
            } else {
                Position(-58.3816, -34.6037) // Buenos Aires
            },
            zoom = 12.0
        )
    )

    val locationProvider = rememberFusedLocationProvider()
    val userLocationState = if (hasPermission) {
        rememberUserLocationState(locationProvider)
    } else {
        null
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude, latitude),
                    zoom = 15.0
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            baseStyle = if (isDarkTheme) {
                BaseStyle.Uri(DARK_STYLE_URL)
            } else {
                BaseStyle.Uri(LIGHT_STYLE_URL)
            },
            onMapClick = { position, _ ->
                onLocationSelected(position.latitude, position.longitude)
                ClickResult.Consume
            },
            options = MapOptions(
                ornamentOptions = OrnamentOptions(
                    isCompassEnabled = true,
                    isScaleBarEnabled = true,
                    isAttributionEnabled = false,
                    isLogoEnabled = false
                )
            )
        ) {
            if (hasPermission) {
                LocationPuck(
                    idPrefix = "user",
                    location = userLocationState?.location,
                    cameraState = cameraState
                )
            }

            if (latitude != null && longitude != null) {
                val selectedLocationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(longitude, latitude)
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
                    color = const(MaterialTheme.colorScheme.primary),
                    radius = const(10.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.dp)
                )
            }
        }

        // Overlay buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasPermission) {
                Button(
                    onClick = {
                        userLocationState?.location?.let {
                            onLocationSelected(
                                it.position.value.latitude,
                                it.position.value.longitude
                            )
                        }
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }
            }
        }
    }
}
