package com.example.gruya.ui.screens.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gruya.ui.components.*
import com.example.gruya.ui.screens.vehicle.VehiclesNavigationEvent
import com.example.gruya.ui.screens.vehicle.VehiclesViewModel

@Composable
fun VehiclesScreen(
    viewModel: VehiclesViewModel = hiltViewModel(),
    onAddVehicle: () -> Unit = {},
    onEditVehicle: (Int) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Mis Vehículos",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gestiona tu flota registrada para asistencia rápida.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Delete confirmation dialog
        if (uiState.value.showDeleteDialog) {
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

        uiState.value.vehicles.forEach { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                onDelete = viewModel::onDeleteClick,
                onEdit = viewModel::onEditClick,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AddVehicleCard(onAddClick = viewModel::onAddVehicle)

        Spacer(modifier = Modifier.height(16.dp))
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
