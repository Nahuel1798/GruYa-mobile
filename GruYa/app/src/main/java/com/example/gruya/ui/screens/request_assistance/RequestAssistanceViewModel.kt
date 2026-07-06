package com.example.gruya.ui.screens.request_assistance

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.gruya.connectivity.ConnectivityObserver
import com.example.gruya.data.local.entity.SyncStatus
import com.example.gruya.data.local.entity.VehicleCacheEntity
import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.VehicleRepository
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.ServiceType
import com.example.gruya.domain.model.Vehicle
import com.example.gruya.domain.model.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RequestAssistanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val assistanceRepository: AssistanceRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestAssistanceUiState())
    val uiState: StateFlow<RequestAssistanceUiState> = _uiState.asStateFlow()

    private val geocoder = Geocoder(context, Locale.getDefault())

    /** Tracks online/offline state for branching in onSubmit(). */
    private var _isOnline: Boolean = true

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        val providerId: Int? = savedStateHandle.get<Int>("providerId")
        val serviceType: String? = savedStateHandle.get<String>("serviceType")
        val initialLat: Double? = savedStateHandle.get<Double>("initialLat")
        val initialLng: Double? = savedStateHandle.get<Double>("initialLng")

        _uiState.update {
            it.copy(
                providerId = providerId,
                serviceType = serviceType,
                selectedIssueType = mapServiceTypeToIssueType(serviceType),
                location = if (initialLat != null && initialLng != null) Pair(initialLat, initialLng) else it.location
            )
        }

        if (initialLat != null && initialLng != null) {
            updateAddress(initialLat, initialLng, isDestination = false)
        }

        // Observe connectivity
        observeConnectivity()

        // Observe cached vehicles for offline use
        observeCachedVehicles()

        // Observe GPS provider changes (retry when user turns on GPS)
        observeGpsProviderChanges()

        // Check GPS availability
        checkGpsState()

        loadVehicles()
    }

    // ── State observation ──────────────────────────────────────────

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                val online = status == ConnectivityObserver.Status.Available
                val wasOffline = !_isOnline
                _isOnline = online
                _uiState.update { it.copy(isOfflineMode = !online) }
                // When offline is detected, try to get GPS location and set destination = origin
                if (!online) {
                    checkGpsState()
                }
                // When connectivity is restored, trigger sync of pending requests
                if (online && wasOffline && _uiState.value.syncStatus == SyncStatus.PENDING) {
                    syncPendingAssistances()
                }
            }
        }
    }

    /**
     * Direct sync call triggered when connectivity is restored.
     * Falls back to WorkManager constraint as well (in enqueueSync).
     */
    private suspend fun syncPendingAssistances() {
        _uiState.update { it.copy(syncStatus = SyncStatus.SYNCING) }
        val result = assistanceRepository.syncPendingAssistances()
        result.fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        syncStatus = SyncStatus.SYNCED,
                        pendingId = null,
                        offlineQueueMessage = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        syncStatus = SyncStatus.FAILED,
                        error = error.message
                    )
                }
            }
        )
    }

    private fun observeCachedVehicles() {
        viewModelScope.launch {
            vehicleRepository.getCachedVehicles().collect { cached ->
                _uiState.update { it.copy(cachedVehicles = cached) }
            }
        }
    }

    // ── GPS provider change polling ────────────────────────────────

    private fun observeGpsProviderChanges() {
        viewModelScope.launch {
            while (true) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val wasEnabled = _uiState.value.isGpsAvailable
                val isEnabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                if (isEnabled != wasEnabled) {
                    checkGpsState()
                }
                delay(2000L)
            }
        }
    }

    /**
     * Called from the Screen's LifecycleResumeEffect to re-check GPS
     * when the user returns from settings (e.g., after enabling GPS).
     */
    fun onResume() {
        checkGpsState()
    }

    private fun checkGpsState() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        _uiState.update { it.copy(isGpsAvailable = isGpsEnabled) }

        // Try to get current GPS location automatically (offline/online)
        if (isGpsEnabled) {
            loadCurrentGpsLocation(locationManager)
        }
    }

    private fun loadCurrentGpsLocation(locationManager: LocationManager?) {
        try {
            // 1. Try cached last known location first — only if fresh (< 2 min)
            val gps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val network = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLocation = gps ?: network
            val now = System.currentTimeMillis()

            if (bestLocation != null && (now - bestLocation.time) < 120_000L) {
                applyLocationFix(bestLocation.latitude, bestLocation.longitude)
                return
            }

            // 2. No fresh cached location — request a new GPS fix
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null // CancellationToken — null means no cancellation
            ).addOnSuccessListener { location ->
                if (location != null) {
                    applyLocationFix(location.latitude, location.longitude)
                }
                // else: still no fix — wait for next provider change broadcast
            }
        } catch (_: SecurityException) {
            // Permission not granted — location stays null
        }
    }

    private fun applyLocationFix(latitude: Double, longitude: Double) {
        onLocationChanged(latitude, longitude)
        // If destination not explicitly set yet, set it = origin
        if (_uiState.value.destinationLocation == null) {
            onDestinationLocationChanged(latitude, longitude)
        }
    }

    // ── Mapping helpers ───────────────────────────────────────────

    private fun mapServiceTypeToIssueType(serviceType: String?): IssueType? {
        return when (serviceType?.uppercase()) {
            "GOMERIA" -> IssueType.NEUMATICO_PINCHADO
            "MECANICO" -> IssueType.FALLA_MOTOR
            "AUXILIO" -> IssueType.NECESITA_REMOLQUE
            else -> null
        }
    }

    private fun mapStringToServiceType(serviceType: String?): ServiceType {
        return when (serviceType?.uppercase()) {
            "GOMERIA" -> ServiceType.GOMERIA
            "MECANICO" -> ServiceType.MECANICO
            else -> ServiceType.AUXILIO
        }
    }

    private fun VehicleCacheEntity.toVehicle(): Vehicle = Vehicle(
        id = id,
        type = try { VehicleType.valueOf(type) } catch (_: Exception) { VehicleType.AUTO },
        licensePlate = licensePlate,
        brand = brand,
        model = model,
        insurance = insurance,
        color = color,
        imageUrl = imageUrl
    )

    // ── Vehicle loading ───────────────────────────────────────────

    fun loadVehicles() {
        viewModelScope.launch {
            val hasVehicles = _uiState.value.vehicles.isNotEmpty()
            if (!hasVehicles) {
                _uiState.update { it.copy(isLoading = true) }
            }
            val vehicles = vehicleRepository.listAll()
            if (vehicles.isNotEmpty()) {
                _uiState.update { it.copy(vehicles = vehicles, isLoading = false) }
            } else {
                // API returned empty — query cached vehicles directly
                val cachedEntities = withContext(Dispatchers.IO) {
                    vehicleRepository.getCachedVehicles().first()
                }
                if (cachedEntities.isNotEmpty()) {
                    _uiState.update {
                        it.copy(vehicles = cachedEntities.map { it.toVehicle() }, isLoading = false)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        _uiState.update { it.copy(selectedVehicleId = vehicleId) }
    }

    fun onIssueTypeSelected(issueType: IssueType) {
        _uiState.update { it.copy(selectedIssueType = issueType) }
    }

    fun onLocationChanged(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(location = Pair(latitude, longitude)) }
        updateAddress(latitude, longitude, isDestination = false)
    }

    fun onDestinationLocationChanged(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(destinationLocation = Pair(latitude, longitude)) }
        updateAddress(latitude, longitude, isDestination = true)
    }

    fun onAddressQueryChanged(query: String) {
        _uiState.update { it.copy(addressQuery = query) }
    }

    fun onDestinationAddressQueryChanged(query: String) {
        _uiState.update { it.copy(destinationAddressQuery = query) }
    }

    fun searchAddress(isDestination: Boolean) {
        val query = if (isDestination) _uiState.value.destinationAddressQuery else _uiState.value.addressQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocationName(query, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        Pair(addr.latitude, addr.longitude)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            _uiState.update { state ->
                if (location != null) {
                    if (isDestination) {
                        state.copy(
                            destinationLocation = location,
                            destinationAddress = query,
                            isLoading = false
                        )
                    } else {
                        state.copy(
                            location = location,
                            address = query,
                            isLoading = false
                        )
                    }
                } else {
                    state.copy(isLoading = false, error = "No se encontró la dirección")
                }
            }
        }
    }

    private fun updateAddress(latitude: Double, longitude: Double, isDestination: Boolean) {
        viewModelScope.launch {
            val addressText = withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        val street = addr.thoroughfare ?: ""
                        val number = addr.subThoroughfare ?: ""
                        val city = addr.locality ?: ""
                        if (street.isNotEmpty()) "$street $number, $city".trim() else addr.getAddressLine(0)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            _uiState.update {
                if (isDestination) {
                    it.copy(destinationAddress = addressText, destinationAddressQuery = addressText ?: "")
                } else {
                    it.copy(address = addressText, addressQuery = addressText ?: "")
                }
            }
        }
    }

    // ── Submit: online vs offline ─────────────────────────────────

    fun onSubmit(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.selectedVehicleId == null) {
            _uiState.update { it.copy(error = "Seleccioná un vehículo") }
            return
        }

        if (state.selectedIssueType == null) {
            _uiState.update { it.copy(error = "Seleccioná un tipo de problema") }
            return
        }

        if (state.location == null) {
            _uiState.update { it.copy(error = "No se pudo obtener la ubicación") }
            return
        }

        // Offline: auto-set destination = origin if not already set
        val destLat = state.destinationLocation?.first ?: state.location.first
        val destLng = state.destinationLocation?.second ?: state.location.second
        if (state.destinationLocation == null) {
            _uiState.update { it.copy(destinationLocation = Pair(destLat, destLng)) }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateAssistanceRequest(
                serviceType = mapStringToServiceType(state.serviceType),
                issueType = state.selectedIssueType,
                vehicleId = state.selectedVehicleId,
                origin = Location(
                    latitude = state.location.first,
                    longitude = state.location.second
                ),
                destination = Location(
                    latitude = destLat,
                    longitude = destLng
                ),
                providerId = state.providerId
            )

            if (_isOnline) {
                submitOnline(request, onSuccess)
            } else {
                submitOffline(request)
            }
        }
    }

    private suspend fun submitOnline(
        request: CreateAssistanceRequest,
        onSuccess: () -> Unit
    ) {
        val result = assistanceRepository.create(request)
        result.fold(
            onSuccess = {
                _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                onSuccess()
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al enviar la solicitud"
                    )
                }
            }
        )
    }

    private suspend fun submitOffline(request: CreateAssistanceRequest) {
        when (val outcome = assistanceRepository.createOffline(request)) {
            is AssistanceRepository.QueueAssistanceOutcome.Queued -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        syncStatus = SyncStatus.PENDING,
                        pendingId = outcome.pendingId,
                        offlineQueueMessage = "Tu solicitud fue guardada. Se enviará automáticamente cuando tengas conexión."
                    )
                }
            }
            is AssistanceRepository.QueueAssistanceOutcome.Failed -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = outcome.error
                    )
                }
            }
            is AssistanceRepository.QueueAssistanceOutcome.Submitted -> {
                // Shouldn't happen in offline path, but handle gracefully
                _uiState.update {
                    it.copy(isLoading = false, isSubmitted = true)
                }
            }
        }
    }

    // ── Manual retry (FAILED / NEEDS_REAUTH) ──────────────────────

    fun retryPendingAssistance(pendingId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncStatus = SyncStatus.PENDING) }
            // Re-enqueue the worker to retry sync
            assistanceRepository.syncPendingAssistances()
        }
    }

    // ── Error handling ────────────────────────────────────────────

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
