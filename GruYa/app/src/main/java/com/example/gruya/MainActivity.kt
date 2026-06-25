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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gruya.connectivity.ConnectivityObserver
import com.example.gruya.ui.screens.common.NoInternetScreen
import com.example.gruya.domain.model.Role
import com.example.gruya.ui.NotificationViewModel
import com.example.gruya.ui.navigation.AppDest
import com.example.gruya.ui.navigation.EXTRA_ASSISTANCE_ID
import com.example.gruya.ui.navigation.EXTRA_NAV_TYPE
import com.example.gruya.ui.navigation.EXTRA_TRACKING_SESSION_ID
import com.example.gruya.ui.navigation.NavEvent
import com.example.gruya.ui.navigation.NavigationEventBus
import com.example.gruya.ui.navigation.navEventFromExtras
import com.example.gruya.ui.screens.assistances.AssistancesScreen
import com.example.gruya.ui.screens.assistance_tracking.AssistanceTrackingScreen
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
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navEventBus: NavigationEventBus

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

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
                        connectivityObserver = connectivityObserver
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

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent == null) return
        val navType = intent.getStringExtra(EXTRA_NAV_TYPE)
        val assistanceId = intent.getIntExtra(EXTRA_ASSISTANCE_ID, -1)
        val trackingSessionId = intent.getStringExtra(EXTRA_TRACKING_SESSION_ID)
        if (navType != null && assistanceId > 0) {
            val event = navEventFromExtras(navType, assistanceId, trackingSessionId)
            if (event != null) {
                navEventBus.emit(event)
            }
        }
    }
}

@Composable
fun GruYaApp(
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    navEventBus: NavigationEventBus,
    connectivityObserver: ConnectivityObserver
) {
    val connectivityFlow = remember { connectivityObserver.observe() }
    val status by connectivityFlow.collectAsState(initial = ConnectivityObserver.Status.Available)

    if (status != ConnectivityObserver.Status.Available) {
        NoInternetScreen()
        return
    }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isCheckingToken by authViewModel.isCheckingToken.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // State for pending notification navigation to tab destinations
    val pendingNavEvent = remember { mutableStateOf<NavEvent?>(null) }

    // State for custom foreground notification overlay
    val currentNotification = remember { mutableStateOf<ForegroundNotification?>(null) }

    LaunchedEffect(currentNotification.value) {
        if (currentNotification.value != null) {
            delay(5000)
            currentNotification.value = null
        }
    }

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
        navEventBus.events.collect { event ->
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

    // Track foreground state for the NavigationEventBus
    LaunchedEffect(Unit) {
        navEventBus.isForeground = true
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

    if (isCheckingToken) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val backStack = rememberNavBackStack(
        if (isLoggedIn) AppDest.MainContent
        else AppDest.Login
    )

    LaunchedEffect(isLoggedIn) {
        val expected = if (isLoggedIn) AppDest.MainContent else AppDest.Login
        if (backStack.lastOrNull() != expected) {
            backStack.clear()
            backStack.add(expected)
        }
    }

    val providerViewModel: ProviderProfileViewModel = hiltViewModel()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
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
                                backStack.add(AppDest.ProviderProfile)
                            } else {
                                backStack.clear()
                                backStack.add(AppDest.Login)
                            }
                        }
                    )
                }

                entry<AppDest.ProviderProfile> {
                    val providerUiState by providerViewModel.uiState.collectAsState()

                    LaunchedEffect(providerUiState.success) {
                        if (providerUiState.success) {
                            authViewModel.onLoginSuccess()
                            providerViewModel.resetSuccess()
                            backStack.clear()
                            backStack.add(AppDest.MainContent)
                        }
                    }

                    ProviderProfileScreen(
                        uiState = providerUiState,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        },
                        onCompanyNameChange = providerViewModel::onCompanyNameChange,
                        onServiceTypeChange = providerViewModel::onServiceTypeChange,
                        onDescriptionChange = providerViewModel::onDescriptionChange,
                        onAvailableChange = providerViewModel::onAvailableChange,
                        onAddressChange = providerViewModel::onAddressChange,
                        onSearchAddress = providerViewModel::searchAddress,
                        onCurrentLocationChange = providerViewModel::onCurrentLocationChange,
                        onOpenMap = {
                            backStack.add(AppDest.LocationPicker(providerUiState.latitude, providerUiState.longitude))
                        },
                        onConfirm = {
                            providerViewModel.createProfile()
                        },
                        onClearError = providerViewModel::clearError
                    )
                }

                entry<AppDest.LocationPicker> {
                    val currentEntry = backStack.findLast { it is AppDest.LocationPicker } as? AppDest.LocationPicker
                    LocationPickerScreen(
                        initialLat = currentEntry?.initialLat,
                        initialLng = currentEntry?.initialLng,
                        onLocationSelected = { lat, lng ->
                            providerViewModel.onLocationChange(lat, lng)
                            backStack.removeAt(backStack.size - 1)
                        },
                        onBack = {
                            backStack.removeAt(backStack.size - 1)
                        }
                    )
                }

                entry<AppDest.MainContent> {
                    MainNavigationSuite(
                        authViewModel = authViewModel,
                        providerViewModel = providerViewModel,
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
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        notification?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        it.onAction?.invoke()
                        onDismiss()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = it.icon ?: Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = it.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
    is NavEvent.TripStarted -> Icons.Default.DirectionsCar
}

private fun snackbarMessageFor(event: NavEvent): String = when (event) {
    is NavEvent.NewAssistance -> "Nueva solicitud de auxilio"
    is NavEvent.DirectedAssistance -> "Nueva solicitud de auxilio"
    is NavEvent.NewQuote -> "Nueva cotización recibida"
    is NavEvent.QuoteAcceptedProvider -> "Cotización aceptada"
    is NavEvent.QuoteAcceptedClient -> "Cotización aceptada"
    is NavEvent.QuoteRejected -> "Cotización rechazada"
    is NavEvent.TripStarted -> "El proveedor ha iniciado el viaje"
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
    providerViewModel: ProviderProfileViewModel,
    onLogout: () -> Unit,
    pendingNavEvent: MutableState<NavEvent?>
) {
    val tabBackStack = rememberNavBackStack(
        AppDest.TabKey.Home
    )

    val currentRole by authViewModel.currentRole.collectAsState()
    val homeProviderViewModel: HomeProviderViewModel = hiltViewModel()
    val homeProviderUiState by homeProviderViewModel.uiState.collectAsState()

    val showNav = if (currentRole == Role.PROVIDER) {
        homeProviderUiState.isProfileComplete == true
    } else {
        true
    }

    // Handle pending navigation from notification tap
    LaunchedEffect(pendingNavEvent.value) {
        val event = pendingNavEvent.value ?: return@LaunchedEffect
        val dest: NavKey = when (event) {
            is NavEvent.NewAssistance -> AppDest.Quote(event.assistanceId)
            is NavEvent.DirectedAssistance -> AppDest.Quote(event.assistanceId)
            is NavEvent.NewQuote -> AppDest.TabKey.QuotesList(event.assistanceId)
            is NavEvent.QuoteAcceptedProvider -> AppDest.AssistanceTracking(event.assistanceId)
            is NavEvent.QuoteAcceptedClient -> AppDest.TabKey.QuotesList(event.assistanceId)
            is NavEvent.QuoteRejected -> AppDest.TabKey.ProviderQuotes(ProviderQuoteFilter.FINALIZADAS)
            is NavEvent.TripStarted -> AppDest.AssistanceTracking(event.assistanceId, event.trackingSessionId)
        }
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
            Role.PROVIDER -> add(
                NavItem(
                    key = AppDest.TabKey.ProviderQuotes(),
                    label = "Cotizaciones",
                    selectedIcon = Icons.Filled.LocalAtm,
                    unselectedIcon = Icons.Outlined.LocalAtm
                )
            )
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
            if (showNav) {
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
                                }
                            )
                            Role.PROVIDER -> HomeProviderScreen(
                                viewModel = homeProviderViewModel,
                                onNavigateToQuote = { assistanceId ->
                                    tabBackStack.add(AppDest.Quote(assistanceId))
                                },
                                onNavigateToCompleteProfile = {
                                    tabBackStack.add(AppDest.ProviderProfile)
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

                    entry<AppDest.ProviderProfile> {
                        val providerUiState by providerViewModel.uiState.collectAsState()

                        LaunchedEffect(providerUiState.success) {
                            if (providerUiState.success) {
                                providerViewModel.resetSuccess()
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            }
                        }

                        ProviderProfileScreen(
                            uiState = providerUiState,
                            onBack = {
                                if (tabBackStack.size > 1) {
                                    tabBackStack.removeAt(tabBackStack.size - 1)
                                }
                            },
                            onCompanyNameChange = providerViewModel::onCompanyNameChange,
                            onServiceTypeChange = providerViewModel::onServiceTypeChange,
                            onDescriptionChange = providerViewModel::onDescriptionChange,
                            onAvailableChange = providerViewModel::onAvailableChange,
                            onAddressChange = providerViewModel::onAddressChange,
                            onSearchAddress = providerViewModel::searchAddress,
                            onCurrentLocationChange = providerViewModel::onCurrentLocationChange,
                            onOpenMap = {
                                tabBackStack.add(AppDest.LocationPicker(providerUiState.latitude, providerUiState.longitude))
                            },
                            onConfirm = {
                                providerViewModel.createProfile()
                            },
                            onClearError = providerViewModel::clearError
                        )
                    }

                    entry<AppDest.LocationPicker> {
                        val currentEntry = tabBackStack.findLast { it is AppDest.LocationPicker } as? AppDest.LocationPicker
                        LocationPickerScreen(
                            initialLat = currentEntry?.initialLat,
                            initialLng = currentEntry?.initialLng,
                            onLocationSelected = { lat, lng ->
                                providerViewModel.onLocationChange(lat, lng)
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            },
                            onBack = {
                                tabBackStack.removeAt(tabBackStack.size - 1)
                            }
                        )
                    }
                }
            )
        }
    }
}

