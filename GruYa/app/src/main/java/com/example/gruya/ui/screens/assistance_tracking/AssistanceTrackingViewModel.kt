package com.example.gruya.ui.screens.assistance_tracking

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.TrackingRepository
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.data.service.LocationTrackingService
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.Role
import com.example.gruya.domain.model.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import com.example.gruya.domain.model.AssistanceStatus
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssistanceTrackingViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val trackingRepository: TrackingRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistanceTrackingUiState())
    val uiState: StateFlow<AssistanceTrackingUiState> = _uiState.asStateFlow()

    val isTracking: StateFlow<Boolean> = _uiState.map { state ->
        state.trackingState == TrackingState.Tracking ||
        state.trackingState is TrackingState.Connected ||
        (state.assistance?.let { a ->
            !a.trackingSessionId.isNullOrBlank() &&
            a.status != AssistanceStatus.PENDIENTE &&
            a.status != AssistanceStatus.COMPLETADO &&
            a.status != AssistanceStatus.CANCELADO
        } ?: false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val geocoder = Geocoder(context, Locale.getDefault())

    init {
        // Observe tracking state from repository
        viewModelScope.launch {
            trackingRepository.trackingState.collect { state ->
                _uiState.update { it.copy(trackingState = state) }
            }
        }

        // Observe location updates from SignalR
        viewModelScope.launch {
            trackingRepository.locationUpdates.collect { location ->
                _uiState.update { it.copy(providerLocation = location) }
                
                // Throttle route fetch to once every 20 seconds
                val assistanceId = _uiState.value.assistance?.id
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

    fun loadAssistance(assistanceId: Int, providedTrackingSessionId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.getAssistanceDetails(assistanceId)
            result.fold(
                onSuccess = { assistance ->
                    val isProvider = sessionManager.getRole() == Role.PROVIDER
                    _uiState.update {
                        it.copy(
                            assistance = assistance,
                            isLoading = false,
                            isProvider = isProvider
                        )
                    }
                    if (assistance != null) {
                        updateAddresses(assistance)
                        
                        // Use the provided sessionId if the response doesn't have it (e.g., client side via notification)
                        val sessionId = assistance.trackingSessionId ?: providedTrackingSessionId
                        
                        // If already tracking or has a session ID, connect to SignalR
                        // Only connect SignalR for active assistance (not completed/cancelled)
                        val isActiveStatus = assistance.status != AssistanceStatus.COMPLETADO &&
                                             assistance.status != AssistanceStatus.CANCELADO
                        if (!sessionId.isNullOrBlank() && isActiveStatus) {
                            Log.d("AssistanceTrackingVM", "Connecting to tracking session: $sessionId (isProvider: $isProvider)")
                            trackingRepository.connect(sessionId, isProvider = isProvider)
                            if (isProvider) {
                                startLocationService()
                            }
                        } else {
                            Log.d("AssistanceTrackingVM", "No tracking session ID available yet for assistance ${assistance.id}")
                        }
                        getRoute(assistanceId)
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun startTrip() {
        val assistanceId = _uiState.value.assistance?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.startTrip(assistanceId)
            result.fold(
                onSuccess = { tripResponse ->
                    val sessionId = tripResponse?.trackingSessionId
                    if (sessionId != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                assistance = it.assistance?.copy(trackingSessionId = sessionId)
                            )
                        }
                        // Connect to SignalR and start foreground service
                        trackingRepository.connect(sessionId, isProvider = true)
                        startLocationService()
                        getRoute(assistanceId)
                        // Reload assistance to get updated status from server
                        loadAssistance(assistanceId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No se recibió session ID") }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun stopTrip() {
        trackingRepository.disconnect()
        stopLocationService()
        _uiState.update {
            it.copy(
                trackingState = TrackingState.Disconnected,
                providerLocation = null
            )
        }
    }

    fun arriveAtOrigin() {
        val assistanceId = _uiState.value.assistance?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.arriveAtOrigin(assistanceId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    loadAssistance(assistanceId)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun headToDestination() {
        val assistanceId = _uiState.value.assistance?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.headToDestination(assistanceId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    loadAssistance(assistanceId)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun completeService() {
        val assistanceId = _uiState.value.assistance?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = assistanceRepository.completeService(assistanceId)
            result.fold(
                onSuccess = {
                    // Stop tracking on service completion
                    trackingRepository.disconnect()
                    stopLocationService()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trackingState = TrackingState.Disconnected,
                            providerLocation = null
                        )
                    }
                    loadAssistance(assistanceId)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    private val routeMutex = Mutex()
    private var lastRouteFetchTime: Long = 0L

    fun getRoute(assistanceId: Int) {
        if (!routeMutex.tryLock()) return
        viewModelScope.launch {
            try {
                val result = assistanceRepository.getRoute(assistanceId)
                result.fold(
                    onSuccess = { routeResponse ->
                        _uiState.update { state ->
                            state.copy(
                                providerToOriginRoute = routeResponse.providerToOrigin?.geometryJson,
                                assistance = state.assistance?.copy(
                                    routeGeometry = state.assistance.routeGeometry ?: routeResponse.originToDestination?.geometryJson,
                                    distanceKm = routeResponse.providerToOrigin?.distanceKm ?: state.assistance.distanceKm,
                                    etaMinutes = routeResponse.providerToOrigin?.etaMinutes ?: state.assistance.etaMinutes
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("AssistanceTrackingVM", "Error fetching route", error)
                    }
                )
            } finally {
                routeMutex.unlock()
            }
        }
    }

    private fun startLocationService() {
        val currentSessionId = _uiState.value.assistance?.trackingSessionId
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            putExtra("session_id", currentSessionId)
        }
        context.startForegroundService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        context.startService(intent)
    }

    private fun updateAddresses(assistance: AssistanceResponse) {
        viewModelScope.launch {
            val origin = assistance.origin
            val destination = assistance.destination

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(origin.latitude, origin.longitude, 1) { addresses ->
                    val addr = addresses?.firstOrNull()?.getAddressLine(0)
                    if (addr != null) {
                        _uiState.update { it.copy(originAddress = addr) }
                    }
                }
                geocoder.getFromLocation(destination.latitude, destination.longitude, 1) { addresses ->
                    val addr = addresses?.firstOrNull()?.getAddressLine(0)
                    if (addr != null) {
                        _uiState.update { it.copy(destinationAddress = addr) }
                    }
                }
            } else {
                // API < 33: Geocoder.getFromLocation is blocking and can throw IOException
                val (originAddr, destAddr) = withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        val oa = geocoder.getFromLocation(origin.latitude, origin.longitude, 1)?.firstOrNull()?.getAddressLine(0)
                        @Suppress("DEPRECATION")
                        val da = geocoder.getFromLocation(destination.latitude, destination.longitude, 1)?.firstOrNull()?.getAddressLine(0)
                        Pair(oa, da)
                    } catch (e: java.io.IOException) {
                        Log.e("AssistanceTrackingVM", "Geocoder failed on API < 33", e)
                        Pair(null, null)
                    }
                }
                _uiState.update { it.copy(originAddress = originAddr, destinationAddress = destAddr) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingRepository.disconnect()
        stopLocationService()
    }
}
