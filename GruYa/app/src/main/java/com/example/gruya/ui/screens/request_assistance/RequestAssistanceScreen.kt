package com.example.gruya.ui.screens.request_assistance

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Vehicle
import com.example.gruya.domain.model.VehicleType
import com.example.gruya.domain.model.displayName
import com.example.gruya.ui.components.AppTextField
import com.example.gruya.ui.components.VehicleCarouselCard
import com.example.gruya.ui.theme.GruYaTheme

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestAssistanceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMapPicker: (isDestination: Boolean) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    viewModel: RequestAssistanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onNavigateBack()
        }
    }

    // Refresh vehicles when returning from AddVehicle (or any resume)
    LifecycleResumeEffect(Unit) {
        viewModel.loadVehicles()
        onPauseOrDispose { }
    }

    RequestAssistanceContent(
        uiState = uiState,
        onVehicleSelected = viewModel::onVehicleSelected,
        onIssueTypeSelected = viewModel::onIssueTypeSelected,
        onAddressQueryChanged = viewModel::onAddressQueryChanged,
        onDestinationAddressQueryChanged = viewModel::onDestinationAddressQueryChanged,
        onSearchAddress = { viewModel.searchAddress(it) },
        onNavigateToMapPicker = onNavigateToMapPicker,
        onNavigateToAddVehicle = onNavigateToAddVehicle,
        onSubmit = { viewModel.onSubmit(onSuccess = {}) },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestAssistanceContent(
    uiState: RequestAssistanceUiState,
    onVehicleSelected: (Int) -> Unit,
    onIssueTypeSelected: (IssueType) -> Unit,
    onAddressQueryChanged: (String) -> Unit,
    onDestinationAddressQueryChanged: (String) -> Unit,
    onSearchAddress: (Boolean) -> Unit,
    onNavigateToMapPicker: (Boolean) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Solicitar Auxilio",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // --- Vehicle Section ---
            item {
                SectionHeader(
                    title = "Tu Vehículo",
                    actionText = if (uiState.vehicles.isNotEmpty()) "Agregar" else null,
                    onActionClick = onNavigateToAddVehicle
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.vehicles.isEmpty()) {
                    EmptyVehiclesPlaceholder(onNavigateToAddVehicle)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 20.dp)
                    ) {
                        items(uiState.vehicles, key = { it.id }) { vehicle ->
                            VehicleCarouselCard(
                                vehicle = vehicle,
                                isSelected = uiState.selectedVehicleId == vehicle.id,
                                onClick = { onVehicleSelected(vehicle.id) }
                            )
                        }
                    }
                }
            }

            // --- Issue Type Section ---
            item {
                SectionHeader(title = "Tipo de Problema")
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IssueType.entries.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { issueType ->
                                IssueTypeCard(
                                    issueType = issueType,
                                    isSelected = uiState.selectedIssueType == issueType,
                                    onClick = { onIssueTypeSelected(issueType) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // --- Location Section (Route Card) ---
            item {
                SectionHeader(title = "Ubicación y Destino")
                Spacer(modifier = Modifier.height(12.dp))
                RouteSelectionCard(
                    originQuery = uiState.addressQuery,
                    destinationQuery = uiState.destinationAddressQuery,
                    isDestinationEnabled = uiState.location != null,
                    onOriginChange = onAddressQueryChanged,
                    onDestinationChange = onDestinationAddressQueryChanged,
                    onSearch = onSearchAddress,
                    onMapClick = onNavigateToMapPicker
                )
            }

            // --- Submit Button ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Confirmar Solicitud",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun IssueTypeCard(
    issueType: IssueType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = issueTypeIcon(issueType),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = issueType.displayName,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RouteSelectionCard(
    originQuery: String,
    destinationQuery: String,
    isDestinationEnabled: Boolean,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onSearch: (Boolean) -> Unit,
    onMapClick: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Origin
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                AppTextField(
                    value = originQuery,
                    onValueChange = onOriginChange,
                    placeholder = "Tu ubicación actual...",
                    leadingIcon = Icons.Outlined.Search,
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(false)
                        focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        IconButton(onClick = { onMapClick(false) }) {
                            Icon(Icons.Outlined.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Connecting line
            Box(
                modifier = Modifier
                    .padding(start = 9.dp)
                    .height(24.dp)
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // Destination
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = if (isDestinationEnabled) Color(0xFFEF4444) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                AppTextField(
                    value = destinationQuery,
                    onValueChange = onDestinationChange,
                    placeholder = if (isDestinationEnabled) "¿A dónde lo llevamos?" else "Primero fijá el origen",
                    leadingIcon = Icons.Outlined.Search,
                    enabled = isDestinationEnabled,
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(true)
                        focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        IconButton(onClick = { onMapClick(true) }, enabled = isDestinationEnabled) {
                            Icon(
                                Icons.Outlined.Map,
                                contentDescription = null,
                                tint = if (isDestinationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyVehiclesPlaceholder(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No tenés vehículos registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onAddClick) {
                Text("Presioná para agregar uno")
            }
        }
    }
}

private fun issueTypeIcon(issueType: IssueType): ImageVector = when (issueType) {
    IssueType.NEUMATICO_PINCHADO -> Icons.Outlined.Warning
    IssueType.SIN_COMBUSTIBLE -> Icons.Outlined.LocalGasStation
    IssueType.FALLA_MOTOR -> Icons.Outlined.Engineering
    IssueType.NECESITA_REMOLQUE -> Icons.Outlined.LocalShipping
    IssueType.BATERIA_DESCARGADA -> Icons.Outlined.BatteryChargingFull
    IssueType.LLAVE_OLVIDADA -> Icons.Outlined.VpnKey
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun RequestAssistanceContentPreviewDark() {
    GruYaTheme(darkTheme = true) {
        RequestAssistanceContent(
            uiState = RequestAssistanceUiState(
                vehicles = listOf(
                    Vehicle(1, VehicleType.AUTO, "EX 123 AM", "Toyota", "Corolla", "Allianz", "Blanco"),
                    Vehicle(2, VehicleType.MOTO, "EJ 456 EM", "Honda", "CB500", "Federación", "Rojo")
                ),
                selectedVehicleId = 1,
                selectedIssueType = IssueType.NEUMATICO_PINCHADO,
                location = Pair(-34.6037, -58.3816)
            ),
            onVehicleSelected = {},
            onIssueTypeSelected = {},
            onAddressQueryChanged = {},
            onDestinationAddressQueryChanged = {},
            onSearchAddress = {},
            onNavigateToMapPicker = {},
            onNavigateToAddVehicle = {},
            onSubmit = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestAssistanceContentPreviewLight() {
    GruYaTheme(darkTheme = false) {
        RequestAssistanceContent(
            uiState = RequestAssistanceUiState(
                vehicles = listOf(
                    Vehicle(1, VehicleType.AUTO, "EX 123 AM", "Ford", "Fiesta", "La Caja", "Blanco")
                ),
                location = Pair(-34.6037, -58.3816)
            ),
            onVehicleSelected = {},
            onIssueTypeSelected = {},
            onAddressQueryChanged = {},
            onDestinationAddressQueryChanged = {},
            onSearchAddress = {},
            onNavigateToMapPicker = {},
            onNavigateToAddVehicle = {},
            onSubmit = {},
            onNavigateBack = {}
        )
    }
}
