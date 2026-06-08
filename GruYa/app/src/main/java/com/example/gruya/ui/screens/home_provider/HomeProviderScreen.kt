package com.example.gruya.ui.screens.home_provider

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProviderScreen(
    viewModel: HomeProviderViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        "GruYa",
                        color = Color(0xFFFFB95F)
                    )
                },
                actions = {

                    Switch(
                        checked = uiState.isOnline,
                        onCheckedChange = {
                            viewModel.toggleAvailability()
                        }
                    )

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF191C1E)
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101415))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Text(
                    text = "Panel de Operador",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusBadge(uiState.isOnline)
            }

            item {

                StatsSection(
                    services = uiState.todayServices,
                    earnings = uiState.earnings,
                    location = uiState.currentLocation
                )
            }

            item {

                Text(
                    text = "Solicitudes Activas",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            items(
                items = uiState.requests,
                key = { it.id }
            ) { request ->

                RequestCard(
                    request = request,
                    onAccept = {
                        viewModel.acceptRequest(request.id)
                    }
                )
            }

            item {

                CoverageMapCard()
            }
        }
    }
}
@Composable
fun StatusBadge(
    isOnline: Boolean
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1D2022)
        )
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (isOnline)
                            Color.Green
                        else
                            Color.Red,
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text =
                    if (isOnline)
                        "Disponible"
                    else
                        "Desconectado",
                color = Color.White
            )
        }
    }
}

@Composable
fun StatsSection(
    services: Int,
    earnings: Double,
    location: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatCard(
                title = "SERVICIOS HOY",
                value = services.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "GANANCIAS",
                value = "$$earnings",
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B)
            )
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFFFB95F)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    location,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                title,
                color = Color.Gray
            )

            Text(
                value,
                color = Color(0xFFFFB95F),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RequestCard(
    request: ProviderRequestUi,
    onAccept: () -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                request.customerName,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                request.vehicle,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${request.distance} • ${request.eta}",
                color = Color(0xFFFFB95F)
            )

            Text(
                request.address,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAccept
            ) {

                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text("Aceptar Auxilio")
            }
        }
    }
}

@Composable
fun CoverageMapCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Mapa de Cobertura (Google Maps)",
                color = Color.White
            )
        }
    }
}