package com.example.gruya.ui.screens.auth.register

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.gruya.domain.model.ServiceType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
@SuppressLint("MissingPermission")
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
    onCurrentLocationChange: (Double, Double) -> Unit,
    onOpenMap: () -> Unit,
    onConfirm: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        onCurrentLocationChange(it.latitude, it.longitude)
                    }
                }
        }
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    actionColor = MaterialTheme.colorScheme.error
                )
            }
        },
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
                            Icons.AutoMirrored.Filled.ArrowBack,
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

            // Banner de Error Visual
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

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
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = {
                    Text("Descripción")
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
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
                            text = "Disponible para trabajar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Activa para recibir solicitudes en tiempo real",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = uiState.available,
                        onCheckedChange = onAvailableChange
                    )
                }
            }

            Text(
                text = "Ubicación de Base (Local)",
                style = MaterialTheme.typography.labelLarge
            )

            OutlinedTextField(
                value = uiState.address,
                onValueChange = onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = onSearchAddress) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar dirección")
                        }
                        if (uiState.latitude != null && uiState.longitude != null) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Ubicación seleccionada",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                label = {
                    Text("Dirección de base")
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(12.dp)
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
                Text("Seleccionar Base en el Mapa")
            }

            if (uiState.latitude != null && uiState.longitude != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
            }

            // Current Crane Location Section
            Text(
                text = "Ubicación Actual de la Grúa",
                style = MaterialTheme.typography.labelLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (uiState.currentLatitude != null)
                                "Ubicación detectada correctamente"
                            else
                                "Buscando ubicación actual...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (uiState.currentLatitude != null && uiState.currentLongitude != null) {
                        Text(
                            text = "Lat: ${"%.5f".format(uiState.currentLatitude)}, Lng: ${"%.5f".format(uiState.currentLongitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Text(
                text = "PASO FINAL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Configure su perfil profesional",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Indique qué servicios ofrece y dónde opera para que los usuarios puedan encontrarlo fácilmente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else contentColor
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
