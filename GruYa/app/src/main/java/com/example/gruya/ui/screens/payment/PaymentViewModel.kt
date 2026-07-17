package com.example.gruya.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.request.CreatePaymentRequest
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.PaymentRepository
import com.example.gruya.data.repository.QuoteRepository
import com.example.gruya.domain.model.PaymentMethod
import com.example.gruya.domain.model.PaymentStatus
import com.example.gruya.domain.model.QuoteStatus
import com.example.gruya.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val quoteRepository: QuoteRepository,
    private val assistanceRepository: AssistanceRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        val isProvider = sessionManager.getRole() == Role.PROVIDER
        _uiState.update { it.copy(isProvider = isProvider) }
    }

    fun initPayment(assistanceId: Int, amount: Double) {
        if (_uiState.value.assistanceId == assistanceId) return
        
        _uiState.update { 
            it.copy(
                assistanceId = assistanceId,
                amount = amount
            )
        }

        if (amount == 0.0) {
            fetchQuote(assistanceId)
        }
    }

    private fun fetchQuote(assistanceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            quoteRepository.getByAssistance(assistanceId).fold(
                onSuccess = { quotes ->
                    val acceptedQuote = quotes.find { it.status == QuoteStatus.ACEPTADA }
                    if (acceptedQuote != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                amount = acceptedQuote.price
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No se encontró un presupuesto aceptado para esta asistencia"
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al obtener el presupuesto: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    fun pay() {
        val assistanceId = _uiState.value.assistanceId
        val amount = _uiState.value.amount
        val method = _uiState.value.selectedMethod

        if (method == null) {
            _uiState.update { it.copy(error = "Debe seleccionar un método de pago") }
            return
        }

        if (amount <= 0) {
            _uiState.update { it.copy(error = "El monto a pagar debe ser mayor a 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = CreatePaymentRequest(amount, method)
            val payment = paymentRepository.createPayment(assistanceId, request)

            if (payment != null) {
                when (payment.status) {
                    PaymentStatus.PAGADO -> {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true, isFailed = false, payment = payment) }
                    }
                    PaymentStatus.FALLIDO -> {
                        _uiState.update { it.copy(isLoading = false, isSuccess = false, isFailed = true, payment = payment, error = "El pago ha fallado. Por favor, intente con otro método.") }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false, error = "El pago está pendiente de procesamiento.") }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al conectar con el servidor de pagos") }
            }
        }
    }

    fun completeService() {
        val assistanceId = _uiState.value.assistanceId
        if (!_uiState.value.isProvider) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.completeService(assistanceId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isFinished = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al finalizar el servicio: ${error.message}") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, isFailed = false) }
    }

}
