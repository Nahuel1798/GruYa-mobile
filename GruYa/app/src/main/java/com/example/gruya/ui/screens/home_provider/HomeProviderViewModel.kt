package com.example.gruya.ui.screens.home_provider

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.repository.AssistanceRepository
import com.example.gruya.data.repository.NotificationRepository
import com.example.gruya.data.repository.PaymentRepository
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.data.service.ProviderLocationService
import com.example.gruya.ui.navigation.NavEvent
import com.example.gruya.ui.navigation.NavigationEventBus
import com.example.gruya.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.spatialk.geojson.Position

@HiltViewModel
class HomeProviderViewModel @Inject constructor(
    private val assistanceRepository: AssistanceRepository,
    private val providerRepository: ProviderRepository,
    private val paymentRepository: PaymentRepository,
    private val notificationRepository: NotificationRepository,
    private val navigationEventBus: NavigationEventBus,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeProviderUiState())
    val uiState = _uiState.asStateFlow()

    private var syncJob: Job? = null

    init {
        checkProfileCompletion()
        loadNearbyAssistances()
        loadUnreadNotificationsCount()
        loadTodayStats()
        observeAssistanceNotifications()
    }

    fun checkProfileCompletion() {
        viewModelScope.launch {
            providerRepository.getMyProfile()
                .onSuccess { profile ->
                    if (profile != null) {
                        val isFirstCheck = _uiState.value.isProfileComplete == null

                        _uiState.update {
                            it.copy(
                                isProfileComplete = true,
                                providerProfile = profile,
                                isOnline = profile.isAvailable,
                                profileCheckError = null
                            )
                        }

                        // Si el perfil ya está disponible en el backend al iniciar,
                        // nos aseguramos de que el servicio de ubicación esté corriendo (solo para Auxilio).
                        if (isFirstCheck && profile.isAvailable && profile.serviceType == com.example.gruya.domain.model.ServiceType.AUXILIO) {
                            if (hasLocationPermissions()) {
                                val intent = Intent(application, ProviderLocationService::class.java)
                                application.startForegroundService(intent)
                            } else {
                                Log.w("HomeProviderViewModel", "No se puede iniciar el servicio de ubicación: falta permiso")
                                // Si no hay permiso, forzamos el estado offline localmente para ser consistentes
                                // _uiState.update { it.copy(isOnline = false) }
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isProfileComplete = false,
                                profileCheckError = null
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isProfileComplete = null,
                            profileCheckError = "No pudimos verificar tu perfil. Intentá de nuevo en un rato."
                        )
                    }
                }
        }
    }

    fun retryProfileCheck() {
        _uiState.update { it.copy(profileCheckError = null) }
        checkProfileCompletion()
    }

fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted && _uiState.value.isOnline) {
            // Si nos dieron el permiso y deberíamos estar online, iniciamos el servicio
            val intent = Intent(application, ProviderLocationService::class.java)
            application.startForegroundService(intent)
        } else if (!granted && _uiState.value.isOnline) {
            syncJob?.cancel()
            _uiState.update { it.copy(isOnline = false) }
            goOffline()
        }
    }


    fun updateUserLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(userLocation = Position(longitude, latitude))
        }
    }

    fun updateLocationName(name: String) {
        _uiState.update { it.copy(currentLocation = name) }
    }

    fun loadNearbyAssistances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val assistances = assistanceRepository.getNearbyAssistances()
                _uiState.update {
                    it.copy(
                        nearbyAssistances = assistances,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadUnreadNotificationsCount() {
        viewModelScope.launch {
            notificationRepository.getNotifications(1, 50).onSuccess { pagedResponse ->
                val unreadCount = pagedResponse?.data?.count { it.readAt == null } ?: 0
                _uiState.update { it.copy(unreadNotificationsCount = unreadCount) }
            }
        }
    }

    fun loadTodayStats() {
        viewModelScope.launch {
            try {
                val payments = paymentRepository.getMyPayments()
                val todayPayments = payments.filter { 
                    DateTimeUtils.isToday(it.date) && it.status == com.example.gruya.domain.model.PaymentStatus.PAGADO 
                }
                
                val totalEarnings = todayPayments.sumOf { it.amount }
                val servicesCount = todayPayments.size

                _uiState.update {
                    it.copy(
                        todayServices = servicesCount,
                        earnings = totalEarnings
                    )
                }

                // Cargar historial detallado para la nueva vista
                loadEarningsHistory(payments)
            } catch (e: Exception) {
                Log.e("HomeProviderViewModel", "Error loading today stats", e)
            }
        }
    }

    private val assistanceNameCache = mutableMapOf<Int, String>()

    private fun loadEarningsHistory(payments: List<com.example.gruya.domain.model.Payment>) {
        viewModelScope.launch {
            val paymentsWithClient = payments.filter { 
                it.status == com.example.gruya.domain.model.PaymentStatus.PAGADO 
            }.map { payment ->
                val clientName = assistanceNameCache[payment.assistanceId] ?: run {
                    val name = assistanceRepository.getAssistanceDetails(payment.assistanceId)
                        .getOrNull()?.client?.firstName ?: "Cliente"
                    assistanceNameCache[payment.assistanceId] = name
                    name
                }
                PaymentWithClient(payment, clientName)
            }
            _uiState.update { it.copy(paymentsHistory = paymentsWithClient) }
        }
    }

    private fun observeAssistanceNotifications() {
        viewModelScope.launch {
            navigationEventBus.notificationEvents.collect { event ->
                loadUnreadNotificationsCount()
                when (event) {
                    is NavEvent.NewAssistance,
                    is NavEvent.DirectedAssistance,
                    is NavEvent.QuoteRejected -> {
                        Log.d("HomeProviderVM", "Refreshing assistances after $event")
                        loadNearbyAssistances()
                    }
                    is NavEvent.ServiceCompleted -> {
                        Log.d("HomeProviderVM", "Refreshing stats after $event")
                        loadTodayStats()
                    }
                    else -> { /* otros eventos no afectan el mapa de asistencias */ }
                }
            }
        }
    }

    fun toggleAvailability() {
        syncJob?.cancel()
        val goingOnline = !_uiState.value.isOnline
        val isMobile = _uiState.value.providerProfile?.serviceType == com.example.gruya.domain.model.ServiceType.AUXILIO

        if (goingOnline && isMobile && !hasLocationPermissions()) {
            _uiState.update { it.copy(error = "Se requieren permisos de ubicación para estar en línea") }
            return
        }

        _uiState.update { it.copy(isOnline = goingOnline) }

        if (goingOnline) {
            goOnline()
        } else {
            goOffline()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun goOnline() {
        val isMobile = _uiState.value.providerProfile?.serviceType == com.example.gruya.domain.model.ServiceType.AUXILIO
        
        if (isMobile) {
            if (!hasLocationPermissions()) return
            val intent = Intent(application, ProviderLocationService::class.java)
            application.startForegroundService(intent)
        }

        syncAvailabilityStatus(true)
    }

    private fun goOffline() {
        val intent = Intent(application, ProviderLocationService::class.java)
        application.stopService(intent)
        syncAvailabilityStatus(false)
    }

    private fun syncAvailabilityStatus(available: Boolean) {
        syncJob = viewModelScope.launch {
            providerRepository.updateAvailability(available)
                .onSuccess {
                    Log.d("HomeProviderViewModel", "Disponibilidad sincronizada: $available")
                }
                .onFailure { e ->
                    Log.e("HomeProviderViewModel", "Error al sincronizar disponibilidad: ${e.message}", e)
                    // Revertimos el estado en la UI si falla la sincronización
                    _uiState.update {
                        it.copy(
                            isOnline = !available,
                            error = "Error al sincronizar disponibilidad: ${e.message}"
                        )
                    }
                    // Revertir también el lifecycle del servicio
                    if (available) {
                        // Falló al ir online → detener el servicio
                        application.stopService(Intent(application, ProviderLocationService::class.java))
                    } else {
                        // Falló al ir offline → reactivar el servicio
                        application.startForegroundService(Intent(application, ProviderLocationService::class.java))
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
