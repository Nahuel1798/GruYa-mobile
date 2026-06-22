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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.example.gruya.R
import com.example.gruya.ui.theme.Success
import com.example.gruya.ui.theme.Warning
import com.example.gruya.ui.theme.Info
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.expressions.dsl.*
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.util.ClickResult
import kotlinx.serialization.json.*
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRequestAssistance: (Int?, String?, Double?, Double?) -> Unit = { _, _, _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error de estaciones si existe
    LaunchedEffect(uiState.stationsError) {
        uiState.stationsError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

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
            zoom = 12.0
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
            val lat = location.position.value.latitude
            val lng = location.position.value.longitude

            viewModel.updateUserLocation(
                latitud = lat,
                longitud = lng
            )

            if (!locationCentered) {
                cameraState.animateTo(
                    CameraPosition(
                        target = location.position.value,
                        zoom = 14.0
                    )
                )

                locationCentered = true

                // Cargar grúas cercanas una sola vez con la ubicación obtenida
                viewModel.loadService(lat, lng)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            features = uiState.nearbyTowTrucks.map { provider ->
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(
                                            longitude = provider.longitude,
                                            latitude = provider.latitude
                                        )
                                    ),
                                    properties = buildJsonObject {
                                        put("id", provider.id)
                                        put("name", provider.companyName)
                                        put("serviceType", provider.serviceType.uppercase())
                                    }
                                )
                            }
                        )
                    )
                )

                // Fuel station GeoJSON source
                val fuelStationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = uiState.nearbyFuelStations.map { station ->
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(
                                            longitude = station.longitude,
                                            latitude = station.latitude
                                        )
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

                locationState?.let {
                    LocationTrackingEffect(locationState = it, onLocationChange = {})
                    LocationPuck(
                        idPrefix = "user-location",
                        location = it.location,
                        cameraState = cameraState
                    )
                }

                val auxilioIcon = image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
                val gomeriaIcon = image(painterResource(R.drawable.ic_gomeria), drawAsSdf = true)
                val mecanicoIcon = image(painterResource(R.drawable.ic_mecanico), drawAsSdf = true)
                val fuelStationIcon = image(painterResource(R.drawable.ic_estacionservicio), drawAsSdf = true)

                // Tow truck icons
                SymbolLayer(
                    id = "tow-trucks-icons",
                    source = towTruckSource,
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
                    iconSize = const(1.5f),
                    iconAllowOverlap = const(true),
                    iconIgnorePlacement = const(true),
                    onClick = { features ->
                        val clickedFeature = features.firstOrNull()
                        val id = clickedFeature?.properties?.get("id")?.jsonPrimitive?.intOrNull
                        id?.let { clickedId ->
                            uiState.nearbyTowTrucks.find { it.id == clickedId }?.let { provider ->
                                viewModel.selectProvider(provider)
                            }
                        }
                        ClickResult.Consume
                    }
                )

                // Fuel station icons
                SymbolLayer(
                    id = "fuel-stations-icons",
                    source = fuelStationSource,
                    iconImage = fuelStationIcon,
                    iconColor = const(Color(0xFF4CAF50)),
                    iconSize = const(1.2f),
                    iconAllowOverlap = const(true),
                    iconIgnorePlacement = const(true)
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
                                text = "${uiState.nearbyTowTrucks.size} servicios disponibles cerca de ti",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Pedir auxilio a una grua cercana.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { 
                                    onNavigateToRequestAssistance(
                                        null, 
                                        null, 
                                        uiState.userLocation?.latitude, 
                                        uiState.userLocation?.longitude
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                        }
                    }
                }
            }

            uiState.selectedProvider?.let { provider ->

                var addressText by remember(provider.id) { mutableStateOf("Cargando ubicación...") }

                LaunchedEffect(provider.id) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = withContext(Dispatchers.IO) {
                            geocoder.getFromLocation(provider.latitude, provider.longitude, 1)
                        }
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val street = address.thoroughfare ?: ""
                            val number = address.subThoroughfare ?: ""
                            val city = address.locality ?: ""
                            addressText = if (street.isNotEmpty()) "$street $number, $city" else city
                        } else {
                            addressText = "Ubicación no disponible"
                        }
                    } catch (_: Exception) {
                        addressText = "Lat: ${provider.latitude}, Lng: ${provider.longitude}"
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.clearSelectedProvider()
                    }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {

                        Text(
                            text = provider.companyName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = provider.serviceType.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Fila de Disponibilidad
                            Surface(
                                color = if (provider.isAvailable) Success.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (provider.isAvailable) "DISPONIBLE" else "NO DISPONIBLE",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = if (provider.isAvailable) Success else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = provider.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoRow(
                                icon = Icons.Default.Phone,
                                label = "Contacto",
                                value = provider.phone
                            )
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "Ubicación",
                                value = addressText
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // WhatsApp
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                onClick = {
                                    val url = "https://wa.me/${provider.phone.filter { it.isDigit() }}"
                                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("WhatsApp", style = MaterialTheme.typography.labelLarge)
                            }

                            // Llamar
                            OutlinedButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, "tel:${provider.phone}".toUri())
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Llamar", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Botón Solicitar (Principal)
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            onClick = {
                                viewModel.clearSelectedProvider()
                                onNavigateToRequestAssistance(
                                    provider.id, 
                                    provider.serviceType,
                                    uiState.userLocation?.latitude,
                                    uiState.userLocation?.longitude
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                "SOLICITAR ASISTENCIA",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }   // cierra .let

            
            // Botón de refrescar manual — como en el provider
            IconButton(
                onClick = { viewModel.loadService() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = padding.calculateTopPadding() + 4.dp, end = 8.dp)
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar servicios",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// OpenFreeMap style URLs
private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"