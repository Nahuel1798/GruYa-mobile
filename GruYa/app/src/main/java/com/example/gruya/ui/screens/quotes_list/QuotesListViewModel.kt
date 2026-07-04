package com.example.gruya.ui.screens.quotes_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.QuoteRepository
import com.example.gruya.data.repository.TrackingRepository
import com.example.gruya.domain.model.QuoteStatus
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.TrackingState
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import android.content.Context
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class QuotesListViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val assistanceRepository: AssistanceRepository,
    private val trackingRepository: TrackingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuotesListUiState())
    val uiState: StateFlow<QuotesListUiState> = _uiState.asStateFlow()

    private val routeMutex = Mutex()
    private var lastRouteFetchTime: Long = 0L
    private val geocoder = Geocoder(context, Locale.getDefault())
    private var statusPollingJob: Job? = null

    init {
        // Observe tracking state
        viewModelScope.launch {
            trackingRepository.trackingState.collect { state ->
                _uiState.update { it.copy(trackingState = state) }
            }
        }

        // Observe location updates
        viewModelScope.launch {
            trackingRepository.locationUpdates.collect { location ->
                _uiState.update { it.copy(providerLocation = location) }
                
                // Throttle route fetch
                val acceptedQuote = _uiState.value.quotes.find { it.status == QuoteStatus.ACEPTADA }
                val assistanceId = acceptedQuote?.assistanceId
                val now = System.currentTimeMillis()
                if (assistanceId != null && (now - lastRouteFetchTime) > 20_000L) {
                    lastRouteFetchTime = now
                    getRoute(assistanceId)
                }
            }
        }

        // Observe session ended
        viewModelScope.launch {
            trackingRepository.sessionEnded.collect {
                _uiState.update {
                    it.copy(
                        trackingState = TrackingState.Disconnected,
                        providerLocation = null
                    )
                }
            }
        }
    }

    fun loadQuotes(assistanceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = quoteRepository.getByAssistance(assistanceId)
            result.fold(
                onSuccess = { quotes ->
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                    
                    // If there's an accepted quote, start tracking
                    val acceptedQuote = quotes.find { it.status == QuoteStatus.ACEPTADA }
                    acceptedQuote?.let { quote ->
                        startTracking(quote)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(error = throwable.message, isLoading = false) }
                }
            )
        }
    }

    private fun startTracking(quote: Quote) {
        viewModelScope.launch {
            val assistanceResult = assistanceRepository.getAssistanceDetails(quote.assistanceId)
            assistanceResult.onSuccess { assistance ->
                if (assistance != null) {
                    updateAddresses(assistance.origin, assistance.destination)
                    
                    // Always start polling for status updates (e.g. from ACEPTADA to EN_CAMINO_AL_CLIENTE)
                    startStatusPolling(quote.assistanceId, quote.id)

                    val sessionId = assistance.trackingSessionId
                    if (!sessionId.isNullOrBlank()) {
                        trackingRepository.connect(sessionId, isProvider = false)
                        getRoute(quote.assistanceId)
                    }
                    
                    updateLocalAssistanceStatus(quote.id, assistance.status)
                }
            }
        }
    }

    private fun startStatusPolling(assistanceId: Int, quoteId: Int) {
        statusPollingJob?.cancel()
        statusPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000) // Poll every 5 seconds
                val result = assistanceRepository.getAssistanceDetails(assistanceId)
                result.onSuccess { assistance ->
                    if (assistance != null) {
                        val oldStatus = _uiState.value.quotes.find { it.id == quoteId }?.assistance?.status
                        updateLocalAssistanceStatus(quoteId, assistance.status)
                        
                        // Check if we need to connect to tracking (in case sessionId was null initially)
                        val sessionId = assistance.trackingSessionId
                        val currentTrackingState = _uiState.value.trackingState
                        if (!sessionId.isNullOrBlank() && 
                            (currentTrackingState is TrackingState.Idle || currentTrackingState is TrackingState.Disconnected)) {
                            trackingRepository.connect(sessionId, isProvider = false)
                            getRoute(assistanceId)
                        }

                        // If status changed, we might need a new route
                        if (oldStatus != assistance.status) {
                            getRoute(assistanceId)
                        }

                        // Stop polling if finished or cancelled
                        if (assistance.status == AssistanceStatus.COMPLETADO || 
                            assistance.status == AssistanceStatus.CANCELADO) {
                            statusPollingJob?.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun updateLocalAssistanceStatus(quoteId: Int, status: AssistanceStatus) {
        _uiState.update { state ->
            state.copy(
                quotes = state.quotes.map { q ->
                    if (q.id == quoteId) {
                        var updatedQuote = q.copy(assistance = q.assistance.copy(status = status))
                        // Keep Quote status in sync with Assistance status for completion/cancellation
                        if (status == AssistanceStatus.COMPLETADO) {
                            updatedQuote = updatedQuote.copy(status = QuoteStatus.COMPLETADO)
                        } else if (status == AssistanceStatus.CANCELADO) {
                            updatedQuote = updatedQuote.copy(status = QuoteStatus.CANCELADA)
                        }
                        updatedQuote
                    } else q
                }
            )
        }
    }

    private fun updateAddresses(origin: Location, destination: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val originAddr = geocoder.getFromLocation(origin.latitude, origin.longitude, 1)
                    ?.firstOrNull()?.getAddressLine(0)
                @Suppress("DEPRECATION")
                val destAddr = geocoder.getFromLocation(destination.latitude, destination.longitude, 1)
                    ?.firstOrNull()?.getAddressLine(0)
                
                _uiState.update { it.copy(
                    originAddress = originAddr,
                    destinationAddress = destAddr
                ) }
            } catch (e: Exception) {
                Log.e("QuotesListViewModel", "Geocoder failed", e)
            }
        }
    }

    fun getRoute(assistanceId: Int) {
        if (!routeMutex.tryLock()) return
        lastRouteFetchTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                val result = assistanceRepository.getRoute(assistanceId)
                result.fold(
                    onSuccess = { routeResponse ->
                        val acceptedQuote = _uiState.value.quotes.find { it.assistanceId == assistanceId }
                        val status = acceptedQuote?.assistance?.status

                        _uiState.update { state ->
                            val isHeadingToDestination = status == AssistanceStatus.EN_CAMINO_AL_DESTINO

                            state.copy(
                                providerToOriginRoute = routeResponse.providerToOrigin?.geometryJson,
                                providerToDestinationRoute = routeResponse.providerToDestination?.geometryJson,
                                distanceKm = if (isHeadingToDestination) {
                                    routeResponse.providerToDestination?.distanceKm
                                } else {
                                    routeResponse.providerToOrigin?.distanceKm
                                },
                                etaMinutes = if (isHeadingToDestination) {
                                    routeResponse.providerToDestination?.etaMinutes
                                } else {
                                    routeResponse.providerToOrigin?.etaMinutes
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("QuotesListViewModel", "Error fetching route", error)
                    }
                )
            } finally {
                routeMutex.unlock()
            }
        }
    }

    fun accept(quoteId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = quoteId, error = null) }
            val result = quoteRepository.acceptQuote(quoteId)
            result.fold(
                onSuccess = { updatedQuote ->
                    _uiState.update { state ->
                        state.copy(
                            quotes = state.quotes.map { quote ->
                                if (quote.id == quoteId) updatedQuote
                                else quote
                            },
                            actionLoading = null
                        )
                    }
                    startTracking(updatedQuote)
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(error = throwable.message, actionLoading = null) }
                }
            )
        }
    }

    fun reject(quoteId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = quoteId, error = null) }
            val result = quoteRepository.rejectQuote(quoteId)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            quotes = state.quotes.map { quote ->
                                if (quote.id == quoteId) quote.copy(status = QuoteStatus.RECHAZADA)
                                else quote
                            },
                            actionLoading = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(error = throwable.message, actionLoading = null) }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingRepository.disconnect()
    }
}
