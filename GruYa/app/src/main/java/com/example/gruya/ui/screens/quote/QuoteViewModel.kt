package com.example.gruya.ui.screens.quote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository
) : ViewModel() {

    private var assistanceId: Int? = null

    private val _uiState = MutableStateFlow(QuoteUiState())
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    fun setAssistanceId(id: Int) {
        if (assistanceId == id) return
        assistanceId = id
        loadAssistance()
    }

    private fun loadAssistance() {
        val id = assistanceId
        if (id == null) {
            _uiState.update { it.copy(error = "ID de solicitud no encontrado") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = assistanceRepository.getAssistanceDetails(id)
                
                result.fold(
                    onSuccess = { assistance ->
                        if (assistance != null) {
                            _uiState.update { 
                                it.copy(
                                    assistanceRequest = assistance, 
                                    isLoading = false 
                                ) 
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    error = "No se encontró la solicitud o ya no está disponible", 
                                    isLoading = false 
                                ) 
                            }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar la solicitud: ${e.message}", 
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}", 
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun onPriceChanged(price: String) {
        // Permitir solo números y un punto decimal
        if (price.isEmpty() || price.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(price = price, error = null) }
        }
    }

    fun submitQuote() {
        val currentPrice = _uiState.value.price.toDoubleOrNull()
        if (currentPrice == null || currentPrice <= 0) {
            _uiState.update { it.copy(error = "Ingrese un precio válido") }
            return
        }

        val id = assistanceId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = assistanceRepository.sendQuote(id, currentPrice)
                if (success) {
                    _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al enviar el presupuesto") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
            }
        }
    }

    fun updateAddresses(origin: String?, destination: String?) {
        _uiState.update { 
            it.copy(
                originAddress = origin ?: it.originAddress,
                destinationAddress = destination ?: it.destinationAddress
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
