package com.example.gruya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gruya.domain.model.Role
import com.example.gruya.ui.navigation.AppDest
import com.example.gruya.ui.screens.home_user.HomeScreen
import com.example.gruya.ui.screens.auth.login.LoginScreen
import com.example.gruya.ui.screens.auth.register.LocationPickerScreen
import com.example.gruya.ui.screens.auth.register.ProviderProfileScreen
import com.example.gruya.ui.screens.auth.register.ProviderProfileViewModel
import com.example.gruya.ui.screens.auth.register.RegisterScreen
import com.example.gruya.ui.screens.favorites.FavoritesScreen
import com.example.gruya.ui.screens.home_provider.HomeProviderScreen
import com.example.gruya.ui.screens.vehicle.AddVehicleScreen
import com.example.gruya.ui.screens.vehicle.AddVehicleViewModel
import com.example.gruya.ui.screens.vehicle.VehiclesScreen
import com.example.gruya.ui.screens.profile.ProfileScreen
import com.example.gruya.ui.screens.request_assistance.MapPickerScreen
import com.example.gruya.ui.screens.request_assistance.RequestAssistanceScreen
import com.example.gruya.ui.screens.request_assistance.RequestAssistanceViewModel
import com.example.gruya.ui.theme.GruYaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GruYaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GruYaApp()
                }
            }
        }
    }
}

@Composable
fun GruYaApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isCheckingToken by authViewModel.isCheckingToken.collectAsState()

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
                    onLocationChange = providerViewModel::onLocationChange,
                    onOpenMap = {
                        backStack.add(AppDest.LocationPicker(providerUiState.latitude, providerUiState.longitude))
                    },
                    onConfirm = {
                        providerViewModel.createProfile()
                    }
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
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }
        }
    )
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
    onLogout: () -> Unit
) {
    val tabBackStack = rememberNavBackStack(
        AppDest.TabKey.Home
    )

    val currentRole by authViewModel.currentRole.collectAsState()

    val navItems = buildList {

        add(
            NavItem(
                key = AppDest.TabKey.Home,
                label = "Inicio",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            )
        )

        add(
            NavItem(
                key = AppDest.TabKey.Favourites,
                label = "Favoritos",
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder
            )
        )

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

                val selected =
                    tabBackStack.lastOrNull() == item.key

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
                                onNavigateToRequestAssistance = {
                                    tabBackStack.add(AppDest.RequestAssistance)
                                }
                            )
                            Role.PROVIDER -> HomeProviderScreen()
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

                    entry<AppDest.TabKey.Favourites> {
                        FavoritesScreen()
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
                }
            )
        }
    }
}

