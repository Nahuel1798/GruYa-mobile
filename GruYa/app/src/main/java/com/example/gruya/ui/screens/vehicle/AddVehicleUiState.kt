package com.example.gruya.ui.screens.vehicle

import com.example.gruya.domain.model.VehicleType

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

/**
 * Estado completo del formulario "Agregar / Editar Vehículo".
 *
 * Cada campo del formulario tiene su valor y su mensaje de error asociado.
 * El ViewModel es el único que muta este estado; la pantalla solo lo lee.
 *
 * [isEditMode]     True cuando se edita un vehículo existente (viene con id).
 * [vehicleId]      Id del vehículo en modo edición, null en modo creación.
 * [isLoading]      True mientras se está guardando.
 * [successEvent]   Mensaje efímero tras guardar con éxito.
 * [error]          Error global (red, servidor) para mostrar en Snackbar.
 */
data class AddVehicleUiState(

    // ── Tipo de vehículo ───────────────────────────────────────────────────
    val selectedType: VehicleType = VehicleType.AUTO,

    // ── Campos del formulario ──────────────────────────────────────────────
    val plate: String = "",
    val plateError: String? = null,

    val brand: String = "",
    val brandError: String? = null,

    val model: String = "",
    val modelError: String? = null,

    val insurer: String = "",
    val insurerError: String? = null,

    val color: String = "",
    val colorError: String? = null,

    // ── Estado de la pantalla ──────────────────────────────────────────────
    val isLoading: Boolean = false,
    val error: String? = null,
    val successEvent: String? = null,

    // ── Modo edición ───────────────────────────────────────────────────────
    val isEditMode: Boolean = false,
    val vehicleId: Int? = null
) {
    /** True si todos los campos obligatorios tienen valor y no hay errores. */
    val isFormValid: Boolean
        get() = plate.isNotBlank()
                && brand.isNotBlank()
                && model.isNotBlank()
                && color.isNotBlank()
                && plateError == null
                && brandError == null
                && modelError == null
                && colorError == null
}
