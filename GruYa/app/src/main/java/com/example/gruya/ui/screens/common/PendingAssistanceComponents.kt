package com.example.gruya.ui.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gruya.data.local.entity.SyncStatus
import com.example.gruya.domain.model.IssueType

/**
 * Badge that shows the sync status of a pending assistance request.
 */
@Composable
fun PendingStatusBadge(syncStatus: SyncStatus?) {
    val (label, color) = when (syncStatus) {
        SyncStatus.PENDING -> "Pendiente" to Color(0xFFF59E0B)
        SyncStatus.SYNCING -> "Sincronizando" to Color(0xFF3B82F6)
        SyncStatus.SYNCED -> "Enviado" to Color(0xFF10B981)
        SyncStatus.FAILED -> "Fallido" to Color(0xFFEF4444)
        SyncStatus.NEEDS_REAUTH -> "Sesión expirada" to Color(0xFF8B5CF6)
        null -> "Desconocido" to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

/**
 * Returns the color + description text for a given sync status.
 */
fun pendingStatusLabel(syncStatus: SyncStatus?): Pair<Color, String> {
    return when (syncStatus) {
        SyncStatus.PENDING -> Color(0xFFF59E0B) to "Se enviará automáticamente cuando tengas conexión."
        SyncStatus.SYNCING -> Color(0xFF3B82F6) to "Sincronizando con el servidor..."
        SyncStatus.SYNCED -> Color(0xFF10B981) to "Enviada correctamente al servidor."
        SyncStatus.FAILED -> Color(0xFFEF4444) to "No se pudo enviar. Es posible que ya tengas una solicitud activa. Eliminala para crear una nueva."
        SyncStatus.NEEDS_REAUTH -> Color(0xFF8B5CF6) to "Error de autenticación al enviar. Reintentá; si el error continúa, cerrá sesión y volvé a iniciarla."
        null -> Color.Gray to "Estado desconocido."
    }
}

/**
 * Returns the icon for a given issue type.
 */
fun issueTypeIcon(issueType: IssueType?): ImageVector = when (issueType) {
    IssueType.NEUMATICO_PINCHADO -> Icons.Default.TireRepair
    IssueType.SIN_COMBUSTIBLE -> Icons.Default.LocalGasStation
    IssueType.FALLA_MOTOR -> Icons.Default.Build
    IssueType.NECESITA_REMOLQUE -> Icons.Default.CarRepair
    IssueType.BATERIA_DESCARGADA -> Icons.Outlined.BatteryChargingFull
    IssueType.LLAVE_OLVIDADA -> Icons.Outlined.VpnKey
    null -> Icons.Default.Warning
}
