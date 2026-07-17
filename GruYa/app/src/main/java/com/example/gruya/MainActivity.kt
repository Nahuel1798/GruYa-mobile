package com.example.gruya

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gruya.connectivity.ConnectivityObserver
import com.example.gruya.data.local.dao.PendingAssistanceDao
import com.example.gruya.data.local.dao.VehicleCacheDao
import com.example.gruya.data.local.entity.PendingAssistanceEntity
import com.example.gruya.data.local.entity.SyncStatus
import com.example.gruya.ui.screens.common.NoInternetScreen
import com.example.gruya.domain.model.Role
import com.example.gruya.domain.model.ServiceType
import com.example.gruya.ui.NotificationViewModel
import com.example.gruya.ui.navigation.AppDest
import com.example.gruya.ui.navigation.EXTRA_ASSISTANCE_ID
import com.example.gruya.ui.navigation.EXTRA_NAV_TYPE
import com.example.gruya.ui.navigation.EXTRA_TRACKING_SESSION_ID
import com.example.gruya.ui.navigation.NavEvent
import com.example.gruya.ui.navigation.NavigationEventBus
import com.example.gruya.ui.navigation.destForEvent
import com.example.gruya.ui.navigation.navEventFromExtras
import com.example.gruya.ui.navigation.parseTrackingSessionId
import com.example.gruya.ui.screens.assistances.AssistancesScreen
import com.example.gruya.ui.screens.assistance_tracking.AssistanceTrackingScreen
import com.example.gruya.ui.screens.payment.PaymentScreen
import com.example.gruya.ui.screens.assistance_tracking.AssistanceTrackingScreen
import kotlinx.serialization.json.Json
import com.example.gruya.ui.screens.notifications.NotificationListScreen
import com.example.gruya.ui.screens.auth.login.LoginScreen
import com.example.gruya.ui.screens.auth.register.LocationPickerScreen
import com.example.gruya.ui.screens.auth.register.ProviderProfileScreen
import com.example.gruya.ui.screens.auth.register.ProviderProfileViewModel
import com.example.gruya.ui.screens.auth.register.RegisterScreen
import com.example.gruya.ui.screens.home_provider.HomeProviderScreen
import com.example.gruya.ui.screens.home_provider.HomeProviderViewModel
import com.example.gruya.ui.screens.home_user.HomeScreen
import com.example.gruya.ui.screens.profile.ProfileScreen
import com.example.gruya.ui.screens.provider_quotes.ProviderQuoteFilter
import com.example.gruya.ui.screens.provider_quotes.ProviderQuotesScreen
import com.example.gruya.ui.screens.quote.QuoteScreen
import com.example.gruya.ui.screens.quotes_list.QuotesListScreen
import com.example.gruya.ui.screens.quotes_list.QuotesListViewModel
import com.example.gruya.ui.screens.request_assistance.MapPickerScreen
import com.example.gruya.ui.screens.request_assistance.RequestAssistanceScreen
import com.example.gruya.ui.screens.request_assistance.RequestAssistanceViewModel
import com.example.gruya.ui.screens.vehicle.AddVehicleScreen
import com.example.gruya.ui.screens.vehicle.AddVehicleViewModel
import com.example.gruya.ui.screens.vehicle.VehiclesScreen
import com.example.gruya.ui.theme.GruYaTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navEventBus: NavigationEventBus

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    @Inject
    lateinit var vehicleCacheDao: VehicleCacheDao

    @Inject
    lateinit var pendingAssistanceDao: PendingAssistanceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GruYaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GruYaApp(
                        navEventBus = navEventBus,
                        connectivityObserver = connectivityObserver,
                        vehicleCacheDao = vehicleCacheDao,
                        pendingAssistanceDao = pendingAssistanceDao
                    )
                }
            }
        }
        handleNavigationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        navEventBus.isForeground = true
    }

    override fun onPause() {
        super.onPause()
        navEventBus.isForeground = false
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent == null) return
        val navType = intent.getStringExtra(EXTRA_NAV_TYPE)
        val assistanceId = intent.getIntExtra(EXTRA_ASSISTANCE_ID, -1)
        val trackingSessionId = intent.getStringExtra(EXTRA_TRACKING_SESSION_ID)
        if (navType != null && assistanceId > 0) {
            val event = navEventFromExtras(navType, assistanceId, trackingSessionId)
            if (event != null) {
                navEventBus.emitNavigation(event)
            }
        }
    }
}

@Composable
fun GruYaApp(
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    navEventBus: NavigationEventBus,
    connectivityObserver: ConnectivityObserver,
    vehicleCacheDao: VehicleCacheDao,
    pendingAssistanceDao: PendingAssistanceDao
) {
    val connectivityFlow = remember { connectivityObserver.observe() }
    val status by connectivityFlow.collectAsState(initial = ConnectivityObserver.Status.Available)

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isCheckingToken by authViewModel.isCheckingToken.collectAsState()
    val currentRole by authViewModel.currentRole.collectAsState()
    val isProviderProfileComplete by authViewModel.isProviderProfileComplete.collectAsState()
    val providerProfileError by authViewModel.providerProfileError.collectAsState()

    var hasCachedVehicles by remember { mutableStateOf(true) }
    var pendingAssistances by remember { mutableStateOf<List<PendingAssistanceEntity>>(emptyList()) }

    // Check vehicle cache from Room
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                hasCachedVehicles = vehicleCacheDao.count() > 0
            } catch (_: Exception) {
                hasCachedVehicles = false
            }
        }
    }

    // Observe pending offline assistance requests
    LaunchedEffect(Unit) {
        pendingAssistanceDao.observeAll().collect { list ->
            pendingAssistances = list
        }
    }

    // ── Gate 1: Token check — loading while validating session ──
    // Must be BEFORE backStack to avoid flash of wrong Login screen
    if (isCheckingToken) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ── Gate 1.5: Provider profile check ──
    // If the user is a provider, wait until we know the profile status
    // before initialising the backStack. This avoids flashing MainContent
    // and then immediately navigating to ProviderProfileScreen.
    if (isLoggedIn && currentRole == Role.PROVIDER && isProviderProfileComplete == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                if (providerProfileError != null) {
                    Text(
                        text = "No pudimos verificar tu perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = providerProfileError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { authViewModel.refreshProviderProfile() }) {
                        Text("Reintentar")
                    }
                } else {
                    Text(
                        text = "Verificando perfil...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        return
    }

    // ── BackStack (destination depends on auth + profile state) ──
    val startDest: AppDest = when {
        !isLoggedIn -> AppDest.Login
        currentRole == Role.PROVIDER && isProviderProfileComplete == false -> AppDest.ProviderProfile
        else -> AppDest.MainContent
    }
    val backStack = rememberNavBackStack(startDest)

    LaunchedEffect(isLoggedIn, isProviderProfileComplete) {
        val expected: AppDest = when {
            !isLoggedIn -> AppDest.Login
            currentRole == Role.PROVIDER && isProviderProfileComplete == false -> AppDest.ProviderProfile
            else -> AppDest.MainContent
        }
            if (backStack.lastOrNull() != expected) {
                backStack.clear()
                backStack.add(expected)
            }
    }

    // ── Connectivity-based navigation: replace with NoInternet ↔ MainContent ──
    LaunchedEffect(status) {
        if (status != ConnectivityObserver.Status.Available) {
            val last = backStack.lastOrNull()
            if (last != AppDest.NoInternet && last !is AppDest.RequestAssistance) {
                backStack.clear()
                backStack.add(AppDest.NoInternet)
            }
        } else {
            backStack.removeAll { it is AppDest.NoInternet || it is AppDest.RequestAssistance }
            if (backStack.isEmpty()) {
                val fallback: AppDest = when {
                    !isLoggedIn -> AppDest.Login
                    currentRole == Role.PROVIDER && isProviderProfileComplete == false -> AppDest.ProviderProfile
                    else -> AppDest.MainContent
                }
                backStack.add(fallback)
            }
        }
    }

    // ── Connected: Normal app ──

    val snackbarHostState = remember { SnackbarHostState() }

    // State for pending notification navigation to tab destinations
    val pendingNavEvent = remember { mutableStateOf<NavEvent?>(null) }

    // State for custom foreground notification overlay
    val currentNotification = remember { mutableStateOf<ForegroundNotification?>(null) }

    // El manejo del tiempo ahora se delega al componente Overlay para sincronizar con la barra de progreso

    // Launcher para el permiso de notificaciones (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("FCM", "Permiso de notificaciones concedido")
        } else {
            Log.w("FCM", "Permiso de notificaciones denegado")
        }
    }

    LaunchedEffect(Unit) {
        // Solicitar permiso en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Obtener y loguear el token de FCM para debug
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Error obteniendo el token de FCM", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Token actual de FCM: $token")
        }

        // Collect existing legacy notifications for custom overlay
        notificationViewModel.notifications.collect { (title, body) ->
            currentNotification.value = ForegroundNotification(
                title = title,
                description = body,
                icon = Icons.Default.Notifications
            )
        }
    }

    // Observe NavigationEventBus for notification-driven navigation events
    LaunchedEffect(Unit) {
        navEventBus.notificationEvents.collect { event ->
            currentNotification.value = ForegroundNotification(
                title = "GruYa",
                description = snackbarMessageFor(event),
                icon = iconFor(event),
                onAction = {
                    pendingNavEvent.value = event
                }
            )
        }
    }

    // Observe NavigationEventBus for direct navigation events
    LaunchedEffect(Unit) {
        navEventBus.navigationEvents.collect { event ->
            pendingNavEvent.value = event
        }
    }


    LaunchedEffect(Unit) {
        authViewModel.authEvents.collect { event ->
            when (event) {
                AuthEvent.ForceLogout -> {
                    authViewModel.logout()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val onProviderLocationPicked = remember { mutableStateOf<(Double, Double) -> Unit>({ _, _ -> }) }

            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {

                entry<AppDest.Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            authViewModel.onLoginSuccess()
                            // If provider, kick off profile check for Gate 1.5
                            authViewModel.refreshProviderProfile()
                        },
                        onNavigateToRegister = {
                            backStack.add(AppDest.Register)
                        }
                    )
                }

                entry<AppDest.Register> {
                    RegisterScreen(
                        onRegisterSuccess = { role ->
                            if (role == Role.PROVIDER) {
                                backStack.removeAt(backStack.size - 1)
                                backStack.add(AppDest.ProviderProfile)
                            } else {
                                authViewModel.onLoginSuccess()
                                backStack.clear()
                                backStack.add(AppDest.MainContent)
                            }
                        },
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                    )
                }

                entry<AppDest.ProviderProfile> {
                    val viewModel: ProviderProfileViewModel = hiltViewModel()
                    val providerUiState by viewModel.uiState.collectAsState()

                    onProviderLocationPicked.value = { lat, lng ->
                        viewModel.onLocationChange(lat, lng)
                    }

                    LaunchedEffect(providerUiState.success) {
                        if (providerUiState.success) {
                            authViewModel.onLoginSuccess()
                            authViewModel.markProviderProfileComplete()
                            authViewModel.refreshProviderProfile()
                            viewModel.resetSuccess()
                            backStack.clear()
                            backStack.add(AppDest.MainContent)
                        }
                    }

                    ProviderProfileScreen(
                        uiState = providerUiState,
                        onBack = if (!isLoggedIn) ({
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }) else null,
                        onLogout = if (isLoggedIn) ({ authViewModel.logout() }) else null,
                        onCompanyNameChange = viewModel::onCompanyNameChange,
                        onServiceTypeChange = viewModel::onServiceTypeChange,
                        onDescriptionChange = viewModel::onDescriptionChange,
                        onAvailableChange = viewModel::onAvailableChange,
                        onAddressChange = viewModel::onAddressChange,
                        onSearchAddress = viewModel::searchAddress,
                        onCurrentLocationChange = viewModel::onCurrentLocationChange,
                        onOpenMap = {
                            backStack.add(AppDest.LocationPicker(providerUiState.latitude, providerUiState.longitude))
                        },
                        onConfirm = {
                            viewModel.createProfile()
                        },
                        onClearError = viewModel::clearError
                    )
                }

                entry<AppDest.LocationPicker> {
                    val currentEntry = backStack.findLast { it is AppDest.LocationPicker } as? AppDest.LocationPicker
                    LocationPickerScreen(
                        initialLat = currentEntry?.initialLat,
                        initialLng = currentEntry?.initialLng,
                        onLocationSelected = { lat, lng ->
                            onProviderLocationPicked.value(lat, lng)
                            backStack.removeAt(backStack.size - 1)
                        },
                        onBack = {
                            backStack.removeAt(backStack.size - 1)
                        }
                    )
                }

                entry<AppDest.NoInternet> {
                    val scope = rememberCoroutineScope()
                    NoInternetScreen(
                        onRetry = { /* connectivity will be re-evaluated automatically */ },
                        onRequestAssistance = {
                            backStack.add(AppDest.RequestAssistance())
                        },
                        hasCachedVehicles = hasCachedVehicles,
                        isUser = currentRole == Role.USER,
                        pendingAssistances = pendingAssistances,
                        onDeletePending = { pendingId ->
                            scope.launch {
                                pendingAssistanceDao.deleteById(pendingId)
                            }
                        },
                        onRetryPending = { pendingId ->
                            scope.launch {
                                val entity = pendingAssistanceDao.getById(pendingId)
                                if (entity != null) {
                                    pendingAssistanceDao.updateStatus(
                                        entity.copy(status = SyncStatus.PENDING.name)
                                    )
                                }
                            }
                        }
                    )
                }

                entry<AppDest.RequestAssistance> {
                    val vm: RequestAssistanceViewModel = hiltViewModel()
                    RequestAssistanceScreen(
                        viewModel = vm,
                        onNavigateBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        },
                        onNavigateToMapPicker = { /* no-op — offline: auto current location */ },
                        onNavigateToAddVehicle = { /* no-op — requires internet */ },
                        onNavigateToLogin = {
                            backStack.clear()
                            backStack.add(AppDest.Login)
                        }
                    )
                }

                entry<AppDest.MainContent> {
                    MainNavigationSuite(
                        authViewModel = authViewModel,
                        onLogout = {
                            authViewModel.logout()
                        },
                        pendingNavEvent = pendingNavEvent
                    )
                }
            }
        )

            ForegroundNotificationOverlay(
                notification = currentNotification.value,
                onDismiss = { currentNotification.value = null }
            )
        }
    }
}

data class ForegroundNotification(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val onAction: (() -> Unit)? = null
)

@Composable
fun ForegroundNotificationOverlay(
    notification: ForegroundNotification?,
    onDismiss: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(1f) }

    LaunchedEffect(notification) {
        if (notification != null) {
            offsetY.snapTo(0f)
            progress = 1f
            // Animación de la barra de progreso (15 segundos)
            animate(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
            ) { value, _ ->
                progress = value
            }
            // Al terminar la animación, ocultamos la notificación
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY.value < -80f) { // Umbral para descartar
                            scope.launch {
                                offsetY.animateTo(-500f)
                                onDismiss()
                            }
                        } else {
                            scope.launch { offsetY.animateTo(0f) }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        // Solo permitimos deslizar hacia arriba
                        if (dragAmount < 0 || offsetY.value < 0) {
                            change.consume()
                            scope.launch { offsetY.snapTo(offsetY.value + dragAmount) }
                        }
                    }
                )
            }
    ) {
        notification?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        it.onAction?.invoke()
                        onDismiss()
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = it.icon ?: Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it.description,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    // Barra de progreso de tiempo restante
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        trackColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                }
            }
        }
    }
}

private fun iconFor(event: NavEvent): ImageVector = when (event) {
    is NavEvent.NewAssistance, is NavEvent.DirectedAssistance -> Icons.Default.Assignment
    is NavEvent.NewQuote -> Icons.Default.LocalAtm
    is NavEvent.QuoteAcceptedProvider, is NavEvent.QuoteAcceptedClient -> Icons.Default.CheckCircle
    is NavEvent.QuoteRejected -> Icons.Default.Cancel
    is NavEvent.TripStarted, is NavEvent.ProviderArrived -> Icons.Default.DirectionsCar
    is NavEvent.ProviderHeadingToDestination -> Icons.Default.DirectionsCar
    is NavEvent.ServiceCompleted -> Icons.Default.CheckCircle
}

private fun snackbarMessageFor(event: NavEvent): String = when (event) {
    is NavEvent.NewAssistance -> "Nueva solicitud de auxilio"
    is NavEvent.DirectedAssistance -> "Nueva solicitud de auxilio"
    is NavEvent.NewQuote -> "Nueva cotización recibida"
    is NavEvent.QuoteAcceptedProvider -> "Cotización aceptada"
    is NavEvent.QuoteAcceptedClient -> "Cotización aceptada"
    is NavEvent.QuoteRejected -> "Cotización rechazada"
    is NavEvent.TripStarted -> "El proveedor ha iniciado el viaje"
    is NavEvent.ProviderArrived -> "El proveedor ha llegado al origen"
    is NavEvent.ProviderHeadingToDestination -> "El proveedor se dirige al destino"
    is NavEvent.ServiceCompleted -> "Servicio completado"
}

private data class NavItem(
    val key: AppDest.TabKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainNavigationSuite(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    pendingNavEvent: MutableState<NavEvent?>
) {
    val tabBackStack = rememberNavBackStack(
        AppDest.TabKey.Home
    )

    val currentRole by authViewModel.currentRole.collectAsState()
    val homeProviderViewModel: HomeProviderViewModel = hiltViewModel()
    val homeProviderUiState by homeProviderViewModel.uiState.collectAsState()

    // Handle pending navigation from notification tap
    LaunchedEffect(pendingNavEvent.value) {
        val event = pendingNavEvent.value ?: return@LaunchedEffect
        val dest: NavKey = destForEvent(event, event.assistanceId)
        if (tabBackStack.lastOrNull() != dest) {
            tabBackStack.add(dest)
        }
        pendingNavEvent.value = null
    }

    val navItems = buildList {

        add(
            NavItem(
                key = AppDest.TabKey.Home,
                label = "Inicio",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            )
        )

        when (currentRole) {
            Role.USER -> add(
                NavItem(
                    key = AppDest.TabKey.Assistances,
                    label = "Solicitudes",
                    selectedIcon = Icons.Filled.Assignment,
                    unselectedIcon = Icons.Outlined.Assignment
                )
            )
            Role.PROVIDER -> {
                if (homeProviderUiState.providerProfile?.serviceType == ServiceType.AUXILIO) {
                    add(
                        NavItem(
                            key = AppDest.TabKey.ProviderQuotes(),
                            label = "Cotizaciones",
                            selectedIcon = Icons.Filled.LocalAtm,
                            unselectedIcon = Icons.Outlined.LocalAtm
                        )
                    )
                }
            }
            else -> {}
        }

        if (currentRole == Role.USER) {
            add(
                NavItem(
                    key = AppDest.TabKey.Vehicles,
                    label = "Vehículos",
                    selectedIcon = Icons.Filled.DirectionsCar,
                    unselectedIcon = Icons.Outlined.DirectionsCar
                )
            )
        }

        add(
            NavItem(
                key = AppDest.TabKey.Profile,
                label = "Perfil",
                selectedIcon = Icons.Filled.AccountCircle,
                unselectedIcon = Icons.Outlined.AccountCircle
            )
        )
    }

    val navSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    )

    NavigationSuiteScaffold(
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
        ),
        navigationSuiteItems = {
                navItems.forEach { item ->

                    val selected = when (item.key) {
                        is AppDest.TabKey.ProviderQuotes -> tabBackStack.lastOrNull() is AppDest.TabKey.ProviderQuotes
                        else -> tabBackStack.lastOrNull() == item.key
                    }

                    item(
                        selected = selected,

                        onClick = {
                            if (!selected) {
                                tabBackStack.clear()
                                tabBackStack.add(item.key)
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },

                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },

                        colors = navSuiteItemColors
                    )
                }
        }
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            val onMapLocationPicked = remember { mutableStateOf<(Double, Double, Boolean) -> Unit>({ _, _, _ -> }) }

            NavDisplay(
                backStack = tabBackStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {

                    entry<AppDest.TabKey.Home> {
                        when (currentRole) {
                            Role.USER -> HomeScreen(
                                onNavigateToRequestAssistance = { providerId, serviceType, lat, lng ->
                                    tabBackStack.add(AppDest.RequestAssistance(providerId, serviceType, lat, lng))
                                },
                                onNavigateToNotifications = {
                                    tabBackStack.add(AppDest.Notifications)
                                }
                            )
                            Role.PROVIDER -> HomeProviderScreen(
                                viewModel = homeProviderViewModel,
                                onNavigateToQuote = { assistanceId ->
                                    tabBackStack.add(AppDest.Quote(assistanceId))
                                },
                                onNavigateToNotifications = {
                                    tabBackStack.add(AppDest.Notifications)
                                }
                            )
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    entry<AppDest.TabKey.Assistances> {
                        AssistancesScreen(
                            onNavigateToQuotes = { assistanceId ->
                                tabBackStack.add(AppDest.TabKey.QuotesList(assistanceId))
                            }
                        )
                    }

                    entry<AppDest.TabKey.ProviderQuotes> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.TabKey.ProviderQuotes } as? AppDest.TabKey.ProviderQuotes
                        val initialFilter = currentEntry?.initialFilter

                        ProviderQuotesScreen(
                            initialFilter = initialFilter,
                            onNavigateToTracking = { assistanceId ->
                                tabBackStack.add(AppDest.AssistanceTracking(assistanceId))
                            }
                        )
                    }

                    entry<AppDest.AssistanceTracking> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.AssistanceTracking } as? AppDest.AssistanceTracking
                        val assistanceId = currentEntry?.assistanceId ?: return@entry
                        val trackingSessionId = currentEntry.trackingSessionId
                        AssistanceTrackingScreen(
                            assistanceId = assistanceId,
                            trackingSessionId = trackingSessionId,
                            onNavigateBack = {
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            },
                            onNavigateToPayment = { id, amount ->
                                tabBackStack.add(AppDest.Payment(id, amount))
                            }
                        )
                    }

                    entry<AppDest.Payment> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.Payment } as? AppDest.Payment
                        val assistanceId = currentEntry?.assistanceId ?: return@entry
                        val amount = currentEntry.amount

                        PaymentScreen(
                            assistanceId = assistanceId,
                            amount = amount,
                            onPaymentSuccess = {
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            },
                            onNavigateBack = {
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            }
                        )
                    }

                    entry<AppDest.TabKey.QuotesList> {
                        val quotesListViewModel: QuotesListViewModel = hiltViewModel()
                        val currentEntry = tabBackStack.findLast { it is AppDest.TabKey.QuotesList } as? AppDest.TabKey.QuotesList
                        val assistanceId = currentEntry?.assistanceId ?: return@entry

                        LaunchedEffect(assistanceId) {
                            quotesListViewModel.loadQuotes(assistanceId)
                        }

                        QuotesListScreen(
                            onNavigateBack = {
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            }
                        )
                    }

                    entry<AppDest.TabKey.Vehicles> {
                        VehiclesScreen(
                            onAddVehicle = {
                                tabBackStack.add(AppDest.AddVehicle())
                            },
                            onEditVehicle = { vehicleId ->
                                tabBackStack.add(AppDest.AddVehicle(vehicleId))
                            }
                        )
                    }

                    entry<AppDest.TabKey.Profile> {
                        ProfileScreen(
                            onLogout = onLogout
                        )
                    }

                    entry<AppDest.Notifications> {
                        NotificationListScreen(
                            onNavigateBack = {
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            },
                            onNavigateToNotification = { type, assistanceId, dataJson ->
                                val trackingSessionId = parseTrackingSessionId(dataJson)
                                val event = navEventFromExtras(type, assistanceId, trackingSessionId)
                                if (event != null) {
                                    val dest: NavKey = destForEvent(event, event.assistanceId)
                                    if (tabBackStack.lastOrNull() != dest) {
                                        tabBackStack.add(dest)
                                    }
                                }
                            }
                        )
                    }

                    entry<AppDest.RequestAssistance> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.RequestAssistance } as? AppDest.RequestAssistance
                        val vm: RequestAssistanceViewModel = hiltViewModel()

                        onMapLocationPicked.value = { lat, lng, isDest ->
                            if (isDest) vm.onDestinationLocationChanged(lat, lng)
                            else vm.onLocationChanged(lat, lng)
                        }

                        RequestAssistanceScreen(
                            viewModel = vm,
                            onNavigateBack = {
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            },
                            onNavigateToMapPicker = { isDestination ->
                                val state = vm.uiState.value
                                val location = if (isDestination) {
                                    state.destinationLocation ?: state.location
                                } else {
                                    state.location
                                }
                                tabBackStack.add(AppDest.MapPicker(isDestination, location?.first, location?.second))
                            },
                            onNavigateToAddVehicle = {
                                tabBackStack.add(AppDest.AddVehicle())
                            }
                        )
                    }

                    entry<AppDest.MapPicker> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.MapPicker } as? AppDest.MapPicker
                        MapPickerScreen(
                            initialLocation = if (currentEntry?.initialLat != null && currentEntry.initialLng != null) {
                                Pair(currentEntry.initialLat, currentEntry.initialLng)
                            } else null,
                            title = if (currentEntry?.isDestination == true) "Seleccionar destino" else "Seleccionar origen",
                            showNearby = currentEntry?.isDestination == true,
                            onLocationSelected = { lat, lng ->
                                onMapLocationPicked.value(lat, lng, currentEntry?.isDestination == true)
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            },
                            onNavigateBack = {
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            }
                        )
                    }

                    entry<AppDest.AddVehicle> {
                        val addVehicleViewModel: AddVehicleViewModel = hiltViewModel()
                        val addVehicleUiState by addVehicleViewModel.uiState.collectAsState()

                        val currentEntry = tabBackStack.findLast { it is AppDest.AddVehicle }
                        val vehicleId = (currentEntry as? AppDest.AddVehicle)?.vehicleId

                        LaunchedEffect(vehicleId) {
                            vehicleId?.let { addVehicleViewModel.loadVehicle(it) }
                        }

                        AddVehicleScreen(
                            uiState = addVehicleUiState,
                            onTypeSelected = addVehicleViewModel::onTypeSelected,
                            onPlateChange = addVehicleViewModel::onPlateChange,
                            onBrandChange = addVehicleViewModel::onBrandChange,
                            onModelChange = addVehicleViewModel::onModelChange,
                            onInsurerChange = addVehicleViewModel::onInsurerChange,
                            onColorChange = addVehicleViewModel::onColorChange,
                            onImageUrlChange = addVehicleViewModel::onImageUrlChange,
                            onSave = {
                                addVehicleViewModel.onSave(
                                    onSuccess = {
                                        if (tabBackStack.size > 1) {
                                            tabBackStack.removeAt(tabBackStack.size - 1)
                                        }
                                    }
                                )
                            },
                            onNavigateBack = {
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            }
                        )
                    }

                    entry<AppDest.Quote> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.Quote } as? AppDest.Quote
                        QuoteScreen(
                            assistanceId = currentEntry?.assistanceId,
                            onNavigateBack = {
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            }
                        )
                    }


                }
            )
        }
    }
}

