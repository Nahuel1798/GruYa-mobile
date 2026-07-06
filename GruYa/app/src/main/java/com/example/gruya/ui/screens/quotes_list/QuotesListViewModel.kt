package com.example.gruya.ui.screens.quotes_list

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.QuoteRepository
import com.example.gruya.data.repository.TrackingRepository
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.Quote
import com.example.gruya.domain.model.QuoteStatus
import com.example.gruya.domain.model.TrackingState
import com.example.gruya.utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.geojson.Position
import java.util.Locale
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
        observeTrackingState()
        observeLocationUpdates()
        observeSessionEnded()
    }

    private fun observeTrackingState() {
        viewModelScope.launch {
            trackingRepository.trackingState.collect { state ->
                _uiState.update { it.copy(trackingState = state) }
            }
        }
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            trackingRepository.locationUpdates
                .distinctUntilChanged()
                .collect { location ->
                    _uiState.update { it.copy(providerLocation = location) }
                    
                    // Throttled route fetch
                    val acceptedQuote = _uiState.value.quotes.find { it.status == QuoteStatus.ACEPTADA }
                    val assistanceId = acceptedQuote?.assistanceId
                    val now = System.currentTimeMillis()
                    
                    if (assistanceId != null && (now - lastRouteFetchTime) > 30_000L) {
                        getRoute(assistanceId)
                    }
                }
        }
    }

    private fun observeSessionEnded() {
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
                    
                    val acceptedQuote = quotes.find { it.status == QuoteStatus.ACEPTADA }
                    acceptedQuote?.let { startTracking(it) }
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
                    
                    // Parse assistance route geometry if available
                    val assistancePositions = withContext(Dispatchers.Default) {
                        assistance.routeGeometry?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
                    }
                    _uiState.update { it.copy(assistanceRoutePositions = assistancePositions) }

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
                delay(8000) // Poll every 8 seconds
                val result = assistanceRepository.getAssistanceDetails(assistanceId)
                result.onSuccess { assistance ->
                    if (assistance != null) {
                        val oldStatus = _uiState.value.quotes.find { it.id == quoteId }?.assistance?.status
                        updateLocalAssistanceStatus(quoteId, assistance.status)
                        
                        val sessionId = assistance.trackingSessionId
                        val currentTrackingState = _uiState.value.trackingState
                        if (!sessionId.isNullOrBlank() && 
                            (currentTrackingState is TrackingState.Idle || currentTrackingState is TrackingState.Disconnected)) {
                            trackingRepository.connect(sessionId, isProvider = false)
                        }

                        if (oldStatus != assistance.status) {
                            getRoute(assistanceId)
                        }

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
            val updatedQuotes = state.quotes.map { q ->
                if (q.id == quoteId) {
                    var updatedQuote = q.copy(assistance = q.assistance.copy(status = status))
                    if (status == AssistanceStatus.COMPLETADO) {
                        updatedQuote = updatedQuote.copy(status = QuoteStatus.COMPLETADO)
                    } else if (status == AssistanceStatus.CANCELADO) {
                        updatedQuote = updatedQuote.copy(status = QuoteStatus.CANCELADA)
                    }
                    updatedQuote
                } else q
            }
            if (state.quotes == updatedQuotes) state else state.copy(quotes = updatedQuotes)
        }
    }

    private fun updateAddresses(origin: Location, destination: Location) {
        if (_uiState.value.originAddress != null && _uiState.value.destinationAddress != null) return

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
        viewModelScope.launch {
            try {
                lastRouteFetchTime = System.currentTimeMillis()
                val result = assistanceRepository.getRoute(assistanceId)
                result.fold(
                    onSuccess = { routeResponse ->
                        val acceptedQuote = _uiState.value.quotes.find { it.assistanceId == assistanceId }
                        val status = acceptedQuote?.assistance?.status
                        val isHeadingToDestination = status == AssistanceStatus.EN_CAMINO_AL_DESTINO

                        // Parse geometries in background
                        val pToOriginPositions = withContext(Dispatchers.Default) {
                            routeResponse.providerToOrigin?.geometryJson?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
                        }
                        val pToDestPositions = withContext(Dispatchers.Default) {
                            routeResponse.providerToDestination?.geometryJson?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
                        }
                        val assistancePositions = withContext(Dispatchers.Default) {
                            routeResponse.originToDestination?.geometryJson?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
                        }

                        _uiState.update { state ->
                            state.copy(
                                providerToOriginRoute = routeResponse.providerToOrigin?.geometryJson,
                                providerToDestinationRoute = routeResponse.providerToDestination?.geometryJson,
                                providerToOriginPositions = pToOriginPositions,
                                providerToDestinationPositions = pToDestPositions,
                                assistanceRoutePositions = if (assistancePositions.isNotEmpty()) assistancePositions else state.assistanceRoutePositions,
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
        statusPollingJob?.cancel()
        trackingRepository.disconnect()
    }
}
