package com.example.gruya.ui.screens.auth.register

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gruya.domain.model.ServiceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    uiState: ProviderProfileUiState,
    onBack: () -> Unit,
    onCompanyNameChange: (String) -> Unit,
    onServiceTypeChange: (ServiceType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
    onAddressChange: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onLocationChange: (Double, Double) -> Unit,
    onOpenMap: () -> Unit,
    onConfirm: () -> Unit,
    onClearError: () -> Unit
) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            val snackbarJob = launch {
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Indefinite
                )
            }
            delay(10000)
            snackbarJob.cancel()
            onClearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                value = uiState.companyName,
                onValueChange = onCompanyNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre de la empresa")
                },
                placeholder = {
                    Text("Ej: Grúas Express")
                }
            )

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
                trailingIcon = {
                    Row {
                        IconButton(onClick = onSearchAddress) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar dirección")
                        }
                        if (uiState.latitude != null && uiState.longitude != null) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Ubicación seleccionada",
                                tint = Color.Green,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                label = {
                    Text("Dirección")
                }
            )

            Button(
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Seleccionar en el Mapa")
            }

            if (uiState.latitude != null && uiState.longitude != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val cameraState = rememberCameraState(
                        firstPosition = CameraPosition(
                            target = Position(uiState.longitude, uiState.latitude),
                            zoom = 14.0
                        )
                    )

                    LaunchedEffect(uiState.latitude, uiState.longitude) {
                        cameraState.animateTo(
                            CameraPosition(
                                target = Position(uiState.longitude, uiState.latitude),
                                zoom = 14.0
                            )
                        )
                    }

                    MaplibreMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraState = cameraState,
                        baseStyle = if (isSystemInDarkTheme())
                            BaseStyle.Uri("https://tiles.openfreemap.org/styles/dark")
                        else
                            BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty")
                    ) {
                        val markerSource = rememberGeoJsonSource(
                            data = GeoJsonData.Features(
                                geoJson = FeatureCollection(
                                    features = listOf(
                                        Feature(
                                            geometry = Point(longitude = uiState.longitude, latitude = uiState.latitude),
                                            properties = null
                                        )
                                    )
                                )
                            )
                        )

                        CircleLayer(
                            id = "provider-location",
                            source = markerSource,
                            color = const(MaterialTheme.colorScheme.primary),
                            radius = const(8.dp),
                            strokeColor = const(Color.White),
                            strokeWidth = const(2.dp)
                        )
                    }
                }

                Text(
                    text = "Coordenadas: ${uiState.latitude}, ${uiState.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
