package com.example.gruya.ui.screens.assistance_tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.dtos.request.CreatePaymentRequest
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.PaymentRepository
import com.example.gruya.data.repository.QuoteRepository
import com.example.gruya.data.repository.TrackingRepository
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.data.service.LocationTrackingService
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.PaymentMethod
import com.example.gruya.domain.model.QuoteStatus
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
import com.example.gruya.utils.LocationUtils
import com.example.gruya.domain.model.AssistanceStatus
import org.maplibre.spatialk.geojson.Position
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssistanceTrackingViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val trackingRepository: TrackingRepository,
    private val paymentRepository: PaymentRepository,
    private val quoteRepository: QuoteRepository,
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
                // Throttle UI updates for provider location if distance is very small
                val currentState = _uiState.value
                val previousLocation = currentState.providerLocation
                val distance = if (previousLocation != null) {
                    LocationUtils.calculateDistance(previousLocation, location)
                } else {
                    Double.MAX_VALUE
                }

                if (distance > 2.0 || previousLocation == null) {
                    _uiState.update { it.copy(providerLocation = location) }
                }

                val assistanceId = currentState.assistance?.id ?: return@collect
                val now = System.currentTimeMillis()
                
                // Check for deviation to force an immediate redraw using cached positions
                // Offload to background thread
                val isDeviated = withContext(Dispatchers.Default) {
                    val currentPositions = when (currentState.assistance.status) {
                        AssistanceStatus.EN_CAMINO_AL_DESTINO -> currentState.providerToDestinationPositions
                        AssistanceStatus.EN_CAMINO_AL_CLIENTE, AssistanceStatus.ACEPTADA -> currentState.providerToOriginPositions
                        else -> emptyList()
                    }
                    
                    if (currentPositions.isNotEmpty()) {
                        LocationUtils.isDeviated(location, currentPositions)
                    } else false
                }

                // Trigger redraw if deviated OR if 15 seconds have passed (increased from 10)
                if (isDeviated || (now - lastRouteFetchTime) > 15_000L) {
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
                _uiState.value.assistance?.id?.let { id ->
                    loadAssistance(id)
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
                    
                    // Pre-parse assistance route geometry
                    val assistancePositions = withContext(Dispatchers.Default) {
                        assistance?.routeGeometry?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
                    }
                    
                    _uiState.update {
                        it.copy(
                            assistance = assistance,
                            assistanceRoutePositions = assistancePositions,
                            isLoading = false,
                            isProvider = isProvider
                        )
                    }
                    if (assistance != null) {
                        updateAddresses(assistance)
                        fetchAcceptedQuote(assistanceId)
                        fetchPayment(assistanceId)
                        
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
                    _uiState.update { it.copy(isLoading = false, isNearOrigin = false) }
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
                    _uiState.update { it.copy(isLoading = false, isNearOrigin = false) }
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
                        // Parse geometries in background to avoid blocking UI
                        val updates = withContext(Dispatchers.Default) {
                            val providerToOriginDistance = routeResponse.providerToOrigin?.distanceKm
                            val providerToDestinationDistance = routeResponse.providerToDestination?.distanceKm
                            val originToDestinationDistance = routeResponse.originToDestination?.distanceKm
                            
                            val state = _uiState.value
                            val status = state.assistance?.status
                            val isBeyondOrigin = status == AssistanceStatus.EN_ORIGEN || 
                                                status == AssistanceStatus.EN_CAMINO_AL_DESTINO ||
                                                status == AssistanceStatus.COMPLETADO
                            
                            val currentDistance = when {
                                status == AssistanceStatus.EN_CAMINO_AL_DESTINO && providerToDestinationDistance != null -> 
                                    providerToDestinationDistance
                                isBeyondOrigin -> originToDestinationDistance
                                else -> providerToOriginDistance
                            }
                            
                            val currentEta = when {
                                status == AssistanceStatus.EN_CAMINO_AL_DESTINO && routeResponse.providerToDestination?.etaMinutes != null -> 
                                    routeResponse.providerToDestination.etaMinutes
                                isBeyondOrigin -> routeResponse.originToDestination?.etaMinutes
                                else -> routeResponse.providerToOrigin?.etaMinutes
                            }

                            val newProviderToOriginPositions = routeResponse.providerToOrigin?.geometryJson?.let { 
                                LocationUtils.parseRouteGeometry(it) 
                            } ?: if (isBeyondOrigin) emptyList() else state.providerToOriginPositions
                            
                            val newProviderToDestinationPositions = routeResponse.providerToDestination?.geometryJson?.let { 
                                LocationUtils.parseRouteGeometry(it) 
                            } ?: state.providerToDestinationPositions

                            val newAssistanceRoutePositions = routeResponse.originToDestination?.geometryJson?.let { 
                                LocationUtils.parseRouteGeometry(it) 
                            } ?: state.assistanceRoutePositions
                            
                            RouteUpdate(
                                providerToOriginRoute = if (isBeyondOrigin) null else (routeResponse.providerToOrigin?.geometryJson),
                                providerToDestinationRoute = routeResponse.providerToDestination?.geometryJson,
                                providerToOriginPositions = newProviderToOriginPositions,
                                providerToDestinationPositions = newProviderToDestinationPositions,
                                assistanceRoutePositions = newAssistanceRoutePositions,
                                distanceKm = currentDistance,
                                etaMinutes = currentEta,
                                isNearOrigin = providerToOriginDistance != null && providerToOriginDistance <= 0.3,
                                isNearDestination = providerToDestinationDistance != null && providerToDestinationDistance <= 0.3,
                                originToDestinationGeometry = routeResponse.originToDestination?.geometryJson
                            )
                        }

                        _uiState.update { state ->
                            state.copy(
                                providerToOriginRoute = updates.providerToOriginRoute ?: state.providerToOriginRoute,
                                providerToDestinationRoute = updates.providerToDestinationRoute ?: state.providerToDestinationRoute,
                                providerToOriginPositions = updates.providerToOriginPositions,
                                providerToDestinationPositions = updates.providerToDestinationPositions,
                                assistanceRoutePositions = updates.assistanceRoutePositions,
                                assistance = state.assistance?.copy(
                                    routeGeometry = updates.originToDestinationGeometry ?: state.assistance.routeGeometry,
                                    distanceKm = updates.distanceKm ?: state.assistance.distanceKm,
                                    etaMinutes = updates.etaMinutes ?: state.assistance.etaMinutes
                                ),
                                isNearOrigin = updates.isNearOrigin,
                                isNearDestination = updates.isNearDestination
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

    private data class RouteUpdate(
        val providerToOriginRoute: String?,
        val providerToDestinationRoute: String?,
        val providerToOriginPositions: List<Position>,
        val providerToDestinationPositions: List<Position>,
        val assistanceRoutePositions: List<Position>,
        val distanceKm: Double?,
        val etaMinutes: Double?,
        val isNearOrigin: Boolean,
        val isNearDestination: Boolean,
        val originToDestinationGeometry: String?
    )

    private fun startLocationService() {
        if (!hasLocationPermissions()) {
            Log.e("AssistanceTrackingVM", "Cannot start location service: missing permissions")
            return
        }
        val currentSessionId = _uiState.value.assistance?.trackingSessionId
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            putExtra("session_id", currentSessionId)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun fetchAcceptedQuote(assistanceId: Int) {
        viewModelScope.launch {
            val result = quoteRepository.getByAssistance(assistanceId)
            result.onSuccess { quotes ->
                val accepted = quotes.find { it.status == QuoteStatus.ACEPTADA }
                _uiState.update { it.copy(acceptedQuote = accepted) }
            }
        }
    }

    private fun fetchPayment(assistanceId: Int) {
        viewModelScope.launch {
            val payment = paymentRepository.getPaymentByAssistance(assistanceId)
            _uiState.update { it.copy(payment = payment) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingRepository.disconnect()
        stopLocationService()
    }
}
