package com.example.gruya.ui.screens.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gruya.domain.model.VehicleType
import com.example.gruya.ui.components.AppTextField

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

/**
 * Tarjeta de selección de tipo de vehículo.
 */
@Composable
private fun VehicleTypeCard(
    type: VehicleType,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bgColor     = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.label,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = type.label.uppercase(),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// GruYaTextField reemplazado por AppTextField (ui/components/AppTextField.kt)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

/**
 * Pantalla para agregar o editar un vehículo.
 *
 * Es completamente stateless: recibe [uiState] y callbacks.
 * El ViewModel será quien provea el estado y maneje los eventos.
 *
 * @param uiState           Estado actual del formulario.
 * @param onTypeSelected    Usuario seleccionó un tipo de vehículo.
 * @param onPlateChange     Usuario editó la patente.
 * @param onBrandChange     Usuario editó la marca.
 * @param onModelChange     Usuario editó el modelo.
 * @param onInsurerChange   Usuario editó la aseguradora.
 * @param onColorChange     Usuario editó el color.
 * @param onSave            Usuario presionó el botón guardar.
 * @param onNavigateBack    Usuario presionó atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    uiState: AddVehicleUiState,
    onTypeSelected: (VehicleType) -> Unit,
    onPlateChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onInsurerChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Editar Vehículo" else "Agregar Vehículo",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Botón pegado al fondo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isEditMode) "Guardar Cambios" else "Agregar Vehículo",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Tipo de vehículo ───────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Tipo de Vehículo")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val typeIcons = mapOf(
                        VehicleType.AUTO     to Icons.Outlined.DirectionsCar,
                        VehicleType.CAMIONETA to Icons.Outlined.LocalShipping,
                        VehicleType.MOTO     to Icons.Outlined.TwoWheeler
                    )
                    VehicleType.entries.forEach { type ->
                        VehicleTypeCard(
                            type = type,
                            icon = typeIcons[type] ?: Icons.Outlined.DirectionsCar,
                            isSelected = uiState.selectedType == type,
                            onClick = { onTypeSelected(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Patente ────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Patente")
                AppTextField(
                    value = uiState.plate,
                    onValueChange = onPlateChange,
                    placeholder = "Ej: AA 123 BB",
                    leadingIcon = Icons.Outlined.Badge,
                    errorMessage = uiState.plateError
                )
            }

            // ── Marca + Modelo ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionLabel("Marca")
                    AppTextField(
                        value = uiState.brand,
                        onValueChange = onBrandChange,
                        placeholder = "Ej: Toyota",
                        leadingIcon = Icons.Outlined.Sell,
                        errorMessage = uiState.brandError,
                        capitalization = KeyboardCapitalization.Words
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionLabel("Modelo")
                    AppTextField(
                        value = uiState.model,
                        onValueChange = onModelChange,
                        placeholder = "Ej: Hilux",
                        leadingIcon = Icons.Outlined.Settings,
                        errorMessage = uiState.modelError,
                        capitalization = KeyboardCapitalization.Words
                    )
                }
            }

            // ── Aseguradora ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Aseguradora")
                AppTextField(
                    value = uiState.insurer,
                    onValueChange = onInsurerChange,
                    placeholder = "Ej: La Caja Seguros",
                    leadingIcon = Icons.Outlined.Shield,
                    errorMessage = uiState.insurerError,
                    capitalization = KeyboardCapitalization.Words
                )
            }

            // ── Color ──────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Color")
                AppTextField(
                    value = uiState.color,
                    onValueChange = onColorChange,
                    placeholder = "Ej: Blanco Nácar",
                    leadingIcon = Icons.Outlined.Palette,
                    errorMessage = uiState.colorError,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

// Sobrecarga para pasar modifier a VehicleTypeCard desde el Row
@Composable
private fun VehicleTypeCard(
    type: VehicleType,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor  = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bgColor      = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.label,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = type.label.uppercase(),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF12131F, showSystemUi = true)
@Composable
private fun AddVehicleScreenPreview() {
    AddVehicleScreen(
        uiState = AddVehicleUiState(
            selectedType = VehicleType.AUTO,
            plate = "",
            brand = "",
            model = ""
        ),
        onTypeSelected  = {},
        onPlateChange   = {},
        onBrandChange   = {},
        onModelChange   = {},
        onInsurerChange = {},
        onColorChange   = {},
        onSave          = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12131F, showSystemUi = true)
@Composable
private fun AddVehicleScreenWithErrorsPreview() {
    AddVehicleScreen(
        uiState = AddVehicleUiState(
            selectedType = VehicleType.MOTO,
            plate = "ZZ",
            plateError = "Formato inválido. Ej: AA 123 BB",
            brand = "Honda",
            model = ""
        ),
        onTypeSelected  = {},
        onPlateChange   = {},
        onBrandChange   = {},
        onModelChange   = {},
        onInsurerChange = {},
        onColorChange   = {},
        onSave          = {}
    )
}
