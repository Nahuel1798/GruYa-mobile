package com.example.gruya.ui.screens.home_provider

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.graphics.vector.ImageVector
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.gruya.ui.components.AppTopAppBar
import com.example.gruya.domain.model.ServiceType
import com.example.gruya.domain.model.ProviderProfile
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.layers.SymbolLayer
import androidx.compose.ui.res.painterResource
import com.example.gruya.R
import org.maplibre.compose.expressions.dsl.*
import org.maplibre.compose.expressions.value.SymbolAnchor

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProviderScreen(
    viewModel: HomeProviderViewModel = hiltViewModel(),
    onNavigateToQuote: (Int) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.checkProfileCompletion()
        viewModel.loadUnreadNotificationsCount()
        onPauseOrDispose { }
    }

    if (uiState.profileCheckError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "No pudimos verificar tu perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.profileCheckError ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { viewModel.retryProfileCheck() }) {
                    Text("Reintentar")
                }
            }
        }
        return
    }

    if (uiState.isProfileComplete == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    val isStatic = uiState.providerProfile?.serviceType != ServiceType.AUXILIO

    // Location State logic similar to HomeScreen
    val locationProvider = rememberFusedLocationProvider()
    val locationState = if (uiState.hasLocationPermission && !isStatic) {
        rememberUserLocationState(locationProvider = locationProvider)
    } else {
        null
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        viewModel.onLocationPermissionChanged(granted)
    }

    LaunchedEffect(Unit) {
        if (!isStatic) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            locationPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    var hasLoadedAssistances by remember { mutableStateOf(false) }

    LaunchedEffect(locationState?.location) {
        locationState?.location?.let { location ->
            val lat = location.position.value.latitude
            val lng = location.position.value.longitude

            viewModel.updateUserLocation(
                latitude = lat,
                longitude = lng
            )

            // Reverse Geocoding to get the most accurate address
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()?.let {
                            it.getAddressLine(0) ?: it.locality ?: "Ubicación desconocida"
                        } ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                        viewModel.updateLocationName(address)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val address = addresses?.firstOrNull()?.let {
                        it.getAddressLine(0) ?: it.locality ?: "Ubicación desconocida"
                    } ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                    viewModel.updateLocationName(address)
                }
            } catch (_: Exception) {
                viewModel.updateLocationName("${"%.4f".format(lat)}, ${"%.4f".format(lng)}")
            }

            // Trigger load solo la primera vez que se obtiene ubicación
            if (!hasLoadedAssistances) {
                viewModel.loadNearbyAssistances()
                hasLoadedAssistances = true
            }
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetPeekHeight = 200.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        topBar = {
            AppTopAppBar(
                title = "GruYa",
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                titleColor = MaterialTheme.colorScheme.primary,
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (uiState.isOnline) 
                                Color(0xFF4CAF50).copy(alpha = 0.5f) 
                            else 
                                MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (uiState.isOnline) Color(0xFF4CAF50) else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isOnline) "En línea" else "Desconectado",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (uiState.isOnline) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.isOnline,
                                onCheckedChange = { viewModel.toggleAvailability() },
                                modifier = Modifier.scale(0.7f)
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationsCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (uiState.unreadNotificationsCount > 9) "9+" else uiState.unreadNotificationsCount.toString()
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        sheetContent = {
            val profile = uiState.providerProfile
            if (profile != null && profile.serviceType != ServiceType.AUXILIO) {
                StaticProviderSheetContent(profile = profile)
            } else {
                MobileProviderSheetContent(
                    uiState = uiState,
                    onNavigateToQuote = onNavigateToQuote
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            FullCoverageMap(
                assistances = uiState.nearbyAssistances,
                userLocationState = locationState,
                providerProfile = uiState.providerProfile,
                onRefresh = { viewModel.loadNearbyAssistances() },
                onAssistanceClick = onNavigateToQuote
            )
        }
    }
}

@Composable
fun StaticProviderSheetContent(
    profile: ProviderProfile
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = profile.companyName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = profile.serviceType.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ubicación del establecimiento",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sobre nosotros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = profile.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified // Default is fine
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun MobileProviderSheetContent(
    uiState: HomeProviderUiState,
    onNavigateToQuote: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Barra de arrastre (visual)
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Servicios hoy",
                        value = uiState.todayServices.toString(),
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Ganancias",
                        value = "$${uiState.earnings}",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ubicación actual",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.currentLocation.ifBlank { "Buscando ubicación..." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Solicitudes Cercanas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.nearbyAssistances.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay solicitudes en tu zona",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(uiState.nearbyAssistances) { assistance ->
                AssistanceRequestCard(
                    assistance = assistance,
                    onClick = { onNavigateToQuote(assistance.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
@Composable
fun AssistanceRequestCard(
    assistance: NearbyAssistanceResponse,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(assistance.serviceType) {
                            "AUXILIO" -> Icons.Default.LocalShipping
                            "MECANICO" -> Icons.Default.Engineering
                            "GOMERIA" -> Icons.Default.Build
                            else -> Icons.Default.DirectionsCar
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assistance.clientName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = assistance.vehicle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (assistance.isDirected) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "DIRECTO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assistance.issueType ?: "Asistencia técnica",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${"%.1f".format(assistance.distanceKm)} km",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun FullCoverageMap(
    assistances: List<NearbyAssistanceResponse>,
    userLocationState: org.maplibre.compose.location.UserLocationState? = null,
    providerProfile: ProviderProfile? = null,
    onRefresh: () -> Unit = {},
    onAssistanceClick: (Int) -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    var locationCentered by remember { mutableStateOf(false) }
    var selectedAssistance by remember { mutableStateOf<NearbyAssistanceResponse?>(null) }

    val isStatic = providerProfile != null && providerProfile.serviceType != ServiceType.AUXILIO

    val initialPosition = remember(assistances, providerProfile) {
        if (isStatic) {
            Position(providerProfile.longitude, providerProfile.latitude)
        } else if (assistances.isNotEmpty()) {
            Position(assistances[0].origin.longitude, assistances[0].origin.latitude)
        } else {
            Position(-66.3356, -33.2950)
        }
    }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = initialPosition,
            zoom = if (isStatic) 15.0 else 11.0
        )
    )

    // Center on user location once (only for mobile providers)
    LaunchedEffect(userLocationState?.location) {
        if (!locationCentered && !isStatic) {
            userLocationState?.location?.let { location ->
                cameraState.animateTo(
                    CameraPosition(
                        target = location.position.value,
                        zoom = 12.0
                    )
                )
                locationCentered = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            baseStyle = if (isDarkTheme) {
                BaseStyle.Uri(DARK_STYLE_URL)
            } else {
                BaseStyle.Uri(LIGHT_STYLE_URL)
            },
            onMapClick = { _, _ ->
                selectedAssistance = null
                ClickResult.Pass
            }
        ) {
            if (!isStatic) {
                userLocationState?.let {
                    LocationTrackingEffect(locationState = it, onLocationChange = {})
                    LocationPuck(
                        idPrefix = "provider-location",
                        location = it.location,
                        cameraState = cameraState
                    )
                }
            }

            if (isStatic) {
                val establishmentSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(
                                            longitude = providerProfile.longitude,
                                            latitude = providerProfile.latitude
                                        )
                                    ),
                                    properties = buildJsonObject { }
                                )
                            )
                        )
                    )
                )

                val icon = when (providerProfile.serviceType) {
                    ServiceType.GOMERIA -> image(painterResource(R.drawable.ic_gomeria), drawAsSdf = true)
                    ServiceType.MECANICO -> image(painterResource(R.drawable.ic_mecanico), drawAsSdf = true)
                    else -> image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
                }

                SymbolLayer(
                    id = "establishment-marker",
                    source = establishmentSource,
                    iconImage = icon,
                    iconColor = const(MaterialTheme.colorScheme.primary),
                    iconSize = const(1.5f),
                    iconAllowOverlap = const(true),
                    iconAnchor = const(SymbolAnchor.Bottom)
                )
            }

            if (!isStatic) {
                val assistanceSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = assistances.map { assistance ->
                                Feature(
                                    geometry = Point(
                                        coordinates = Position(
                                            longitude = assistance.origin.longitude,
                                            latitude = assistance.origin.latitude
                                        )
                                    ),
                                    properties = buildJsonObject {
                                        put("id", assistance.id)
                                        put("type", assistance.serviceType)
                                    }
                                )
                            }
                        )
                    )
                )

                val assistanceIcon = image(painterResource(R.drawable.ic_destino), drawAsSdf = true)

                SymbolLayer(
                    id = "assistance-markers",
                    source = assistanceSource,
                    iconImage = assistanceIcon,
                    iconColor = const(MaterialTheme.colorScheme.primary),
                    iconSize = const(1.2f),
                    iconAllowOverlap = const(true),
                    iconAnchor = const(SymbolAnchor.Bottom),
                    onClick = { features ->
                        features.firstOrNull()?.properties?.get("id")?.jsonPrimitive?.intOrNull?.let { id ->
                            selectedAssistance = assistances.find { it.id == id }
                        }
                        ClickResult.Consume
                    }
                )
            }
        }

        // Overlay Button for Selected Assistance
        selectedAssistance?.let { assistance ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 220.dp) // Above the bottom sheet peek
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = assistance.clientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${assistance.serviceType} • ${"%.1f".format(assistance.distanceKm)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { onAssistanceClick(assistance.id) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
                    ) {
                        Text("Detalles")
                    }
                }
            }
        }

        // Botón de refrescar manual
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar solicitudes",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
