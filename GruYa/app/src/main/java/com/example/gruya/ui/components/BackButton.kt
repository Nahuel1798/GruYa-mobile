package com.example.gruya.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Botón de navegación "Volver" estandarizado.
 * Usa [Icons.AutoMirrored.Filled.ArrowBack] para ser RTL-aware.
 *
 * @param onClick Callback cuando se presiona.
 * @param modifier Modificador opcional para personalizar posición/tamaño.
 */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver"
        )
    }
}
