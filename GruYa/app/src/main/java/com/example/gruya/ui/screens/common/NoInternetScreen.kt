package com.example.gruya.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gruya.connectivity.ConnectivityGuide
import com.example.gruya.connectivity.MechanicalGuideDetail
import com.example.gruya.connectivity.MechanicalGuideIndex
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun NoInternetScreen(
    onRetry: () -> Unit = {},
    onRequestAssistance: () -> Unit = {},
    hasCachedVehicles: Boolean = true,
    isUser: Boolean = false
) {
    val context = LocalContext.current
    var selectedMechanicalGuide by remember { mutableStateOf<MechanicalGuideDetail?>(null) }

    if (selectedMechanicalGuide != null) {
        MechanicalGuideDetailScreen(
            guide = selectedMechanicalGuide!!,
            onBack = { selectedMechanicalGuide = null }
        )
        return
    }

    val connectivityGuides = remember {
        try {
            val jsonString = context.assets.open("data/connectivity_guides.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<ConnectivityGuide>>() {}.type
            Gson().fromJson<List<ConnectivityGuide>>(jsonString, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val mechanicalGuides = remember {
        try {
            val jsonString = context.assets.open("data/mechanical_guides_index.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<MechanicalGuideIndex>>() {}.type
            Gson().fromJson<List<MechanicalGuideIndex>>(jsonString, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SignalWifiOff,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Ups! Sin conexión",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No pudimos conectarnos al servidor. Por favor, verifica tu conexión a internet.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (connectivityGuides.isNotEmpty()) {
            Text(
                text = "Guía de conectividad:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            connectivityGuides.forEach { guide ->
                TroubleshootingStep(
                    icon = getIconForGuide(guide.icon),
                    title = guide.title,
                    description = guide.description
                )
            }
        }

        if (mechanicalGuides.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Guías de auxilio mecánico:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            mechanicalGuides.forEach { index ->
                Card(
                    onClick = {
                        try {
                            val jsonString = context.assets.open("data/${index.fileName}")
                                .bufferedReader()
                                .use { it.readText() }
                            selectedMechanicalGuide = Gson().fromJson(jsonString, MechanicalGuideDetail::class.java)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getMechanicalIcon(index.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = index.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "Solicitar auxilio" — offline assistance request (user-only)
        if (isUser) {
            OutlinedButton(
                onClick = onRequestAssistance,
                modifier = Modifier.fillMaxWidth(),
                enabled = hasCachedVehicles
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solicitar auxilio")
            }

            if (!hasCachedVehicles) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Necesitás conexión al menos una vez para registrar tus vehículos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reintentar conexión")
        }
    }
}

@Composable
fun MechanicalGuideDetailScreen(
    guide: MechanicalGuideDetail,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = guide.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = guide.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pasos a seguir:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        guide.steps.forEachIndexed { idx, step ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "${idx + 1}.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (guide.warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Advertencias",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    guide.warnings.forEach { warning ->
                        Text(
                            text = "• $warning",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

fun getMechanicalIcon(iconName: String): ImageVector {
    return when (iconName) {
        "battery_alert" -> Icons.Default.BatteryAlert
        "tire_repair" -> Icons.Default.TireRepair
        "thermostat" -> Icons.Default.Thermostat
        "local_gas_station" -> Icons.Default.LocalGasStation
        else -> Icons.Default.Warning
    }
}

fun getIconForGuide(iconName: String): ImageVector {
    return when (iconName) {
        "wifi" -> Icons.Default.Wifi
        "airplanemode_off" -> Icons.Default.AirplanemodeInactive
        "refresh" -> Icons.Default.Refresh
        "apps" -> Icons.Default.Apps
        "location_on" -> Icons.Default.LocationOn
        "phone" -> Icons.Default.Phone
        else -> Icons.Default.CheckCircle
    }
}

@Composable
fun TroubleshootingStep(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
