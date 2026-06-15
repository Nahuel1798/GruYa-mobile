package com.example.gruya.ui.screens.request_assistance

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // --- Error Snackbar ---
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // --- Navigate back on success ---
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onNavigateBack()
        }
    }

    RequestAssistanceContent(
        uiState = uiState,
        onVehicleSelected = viewModel::onVehicleSelected,
        onIssueTypeSelected = viewModel::onIssueTypeSelected,
        onAddressQueryChanged = viewModel::onAddressQueryChanged,
        onDestinationAddressQueryChanged = viewModel::onDestinationAddressQueryChanged,
        onSearchAddress = { viewModel.searchAddress(it) },
        onNavigateToMapPicker = onNavigateToMapPicker,
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
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Solicitar Auxilio",
                        fontWeight = FontWeight.SemiBold
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // --- Vehicle Carousel ---
            item {
                Text(
                    text = "Seleccioná tu vehículo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                if (uiState.vehicles.isEmpty()) {
                    Text(
                        text = "No tenés vehículos registrados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

            // --- Issue Type Grid (2 columns) ---
            item {
                Text(
                    text = "Tipo de problema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IssueType.entries.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { issueType ->
                                val isSelected = uiState.selectedIssueType == issueType

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onIssueTypeSelected(issueType) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = issueTypeIcon(issueType),
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = issueType.displayName,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }

                            // Fill remaining space if odd count
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // --- Location Section ---
            item {
                Text(
                    text = "Ubicación de origen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                AppTextField(
                    value = uiState.addressQuery,
                    onValueChange = onAddressQueryChanged,
                    placeholder = "Buscar dirección o seleccionar en mapa...",
                    leadingIcon = Icons.Outlined.Search,
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearchAddress(false)
                        focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        IconButton(onClick = { onNavigateToMapPicker(false) }) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = "Seleccionar en mapa",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }

            // --- Destination Section ---
            item {
                Text(
                    text = "Destino",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                val isLocationConfirmed = uiState.location != null

                AppTextField(
                    value = uiState.destinationAddressQuery,
                    onValueChange = onDestinationAddressQueryChanged,
                    placeholder = if (isLocationConfirmed) "Hacia dónde vamos..." else "Primero seleccioná tu ubicación",
                    leadingIcon = Icons.Outlined.Search,
                    enabled = isLocationConfirmed,
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearchAddress(true)
                        focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        IconButton(
                            onClick = { onNavigateToMapPicker(true) },
                            enabled = isLocationConfirmed
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = "Seleccionar en mapa",
                                tint = if (isLocationConfirmed) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }
                )
            }

            // --- Submit Button ---
            item {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(top = 8.dp),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmar Solicitud",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
                    Vehicle(1, VehicleType.AUTO, "EX 123 AM", "Ejemplo", "Ejemplo", "Ejemplo", "Blanco"),
                    Vehicle(2, VehicleType.MOTO, "EJ 456 EM", "Ejemplo", "Ejemplo", "Ejemplo", "Rojo")
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
                    Vehicle(1, VehicleType.AUTO, "EX 123 AM", "Ejemplo", "Ejemplo", "Ejemplo", "Blanco")
                ),
                location = Pair(-34.6037, -58.3816)
            ),
            onVehicleSelected = {},
            onIssueTypeSelected = {},
            onAddressQueryChanged = {},
            onDestinationAddressQueryChanged = {},
            onSearchAddress = {},
            onNavigateToMapPicker = {},
            onSubmit = {},
            onNavigateBack = {}
        )
    }
}
