package com.example.gruya.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gruya.domain.model.Vehicle
import com.example.gruya.domain.model.VehicleType

/**
 * Compact horizontal card variant of [VehicleCard] for use in a selection carousel.
 *
 * @param vehicle    The vehicle to display.
 * @param isSelected Whether this card is currently selected (shows primary border).
 * @param onClick    Called when the user taps this card.
 */
@Composable
fun VehicleCarouselCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VehicleImagePlaceholder(modifier = Modifier.size(48.dp))

            Text(
                text = "${vehicle.brand} ${vehicle.model}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            PlateBadge(plate = vehicle.licensePlate)

            Text(
                text = vehicle.color,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun VehicleCarouselCardPreview() {
    MaterialTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VehicleCarouselCard(
                vehicle = Vehicle(
                    id = 1,
                    type = VehicleType.AUTO,
                    licensePlate = "EX 123 AM",
                    brand = "Ejemplo",
                    model = "Ejemplo",
                    insurance = "Ejemplo",
                    color = "Blanco"
                ),
                isSelected = true,
                onClick = {}
            )
            VehicleCarouselCard(
                vehicle = Vehicle(
                    id = 2,
                    type = VehicleType.MOTO,
                    licensePlate = "EJ 456 EM",
                    brand = "Ejemplo",
                    model = "Ejemplo",
                    insurance = "Ejemplo",
                    color = "Rojo"
                ),
                isSelected = false,
                onClick = {}
            )
        }
    }
}
