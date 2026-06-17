package com.example.gruya.ui.screens.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.gruya.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: VehiclesViewModel = hiltViewModel(),
    onAddVehicle: () -> Unit = {},
    onEditVehicle: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresca la lista al reanudar la pantalla (vuelta de add/edit)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.listVehicles()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is VehiclesNavigationEvent.AddVehicle -> onAddVehicle()
                is VehiclesNavigationEvent.EditVehicle -> onEditVehicle(event.vehicleId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Vehículos",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddVehicle,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.vehicles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.vehicles.isEmpty()) {
                EmptyVehiclesState(onAddClick = viewModel::onAddVehicle)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    item {
                        Text(
                            text = "Gestiona tu flota registrada para asistencia rápida.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                        )
                    }

                    items(uiState.vehicles) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onDelete = viewModel::onDeleteClick,
                            onEdit = viewModel::onEditClick,
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        AddVehicleCard(onAddClick = viewModel::onAddVehicle)
                    }
                }
            }

            // Delete confirmation dialog
            if (uiState.showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = viewModel::onDismissDialog,
                    title = { Text("Eliminar vehículo") },
                    text = {
                        Text("¿Estás seguro de que deseas eliminar este vehículo?")
                    },
                    confirmButton = {
                        TextButton(onClick = viewModel::confirmDelete) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onDismissDialog) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyVehiclesState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No tienes vehículos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Registra tus vehículos para que podamos ayudarte mejor en caso de emergencia.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar mi primer vehículo")
        }
    }
}

@Preview(
    name = "Mis Vehículos – Dark",
    showBackground = true,
    backgroundColor = 0xFF12131F,
    showSystemUi = true
)
@Composable
private fun MisVehiculosScreenPreview() {
    VehiclesScreen()
}
