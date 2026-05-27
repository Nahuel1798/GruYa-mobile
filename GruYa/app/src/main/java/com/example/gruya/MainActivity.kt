package com.example.gruya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gruya.ui.navigation.AppDest
import com.example.gruya.ui.screens.auth.AuthViewModel
import com.example.gruya.ui.screens.favorites.FavoritesScreen
import com.example.gruya.ui.screens.HomeScreen
import com.example.gruya.ui.screens.auth.login.LoginScreen
import com.example.gruya.ui.screens.ProfileScreen
import com.example.gruya.ui.screens.auth.register.RegisterScreen
import com.example.gruya.ui.theme.GruYaTheme

class MainActivity : ComponentActivity() {

    //private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GruYaTheme {
                GruYaApp()
            }
        }
    }
}

@Composable
fun GruYaApp(authViewModel: AuthViewModel = viewModel()) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val backstack = rememberNavBackStack(
        if (isLoggedIn) AppDest.MainContent else AppDest.Login
    )
    NavDisplay(
        backStack = backstack,
        entryProvider = entryProvider {
            entry<AppDest.Login> {
                LoginScreen(onLoginSuccess = {
                    authViewModel.onLoginSuccess()

                    backstack.clear()
                    backstack.add(AppDest.MainContent)
                },
                    onNavigateToRegister = {
                        backstack.add(AppDest.Register)
                    })
            }
            entry<AppDest.Register> {
                RegisterScreen(onRegisterSuccess = {
                    backstack.clear()
                    backstack.add(AppDest.Login)
                })
            }
            entry<AppDest.MainContent> {
                MainNavigationSuite(onLogout = {
                    authViewModel.logout()
                    backstack.clear()
                    backstack.add(AppDest.Login)
                })
            }
        }
    )
}

@Composable
fun MainNavigationSuite(onLogout: () -> Unit) {
    val tabBackStack = rememberNavBackStack(AppDest.TabKey.Home)
    NavigationSuiteScaffold(

        navigationSuiteItems = {
            val tabs = listOf(
                Triple(AppDest.TabKey.Home, "Inicio", Icons.Default.Home),
                Triple(AppDest.TabKey.Favourites, "Favoritos", Icons.Default.Favorite),
                Triple(AppDest.TabKey.Profile, "Perfil", Icons.Default.AccountCircle),
            )

            tabs.forEach { (key, label, icon) ->
                item(
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    selected = tabBackStack.lastOrNull() == key,
                    onClick = {
                        tabBackStack.clear()
                        tabBackStack.add(key)
                    }
                )
            }
        }
    ) {
        NavDisplay(
            backStack = tabBackStack,
            entryProvider = entryProvider {
                entry<AppDest.TabKey.Home> { HomeScreen() }
                entry<AppDest.TabKey.Favourites> { FavoritesScreen() }
                entry<AppDest.TabKey.Profile> { ProfileScreen() }
            }
        )
    }
}



/*
@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
fun GruYaAppPreview() {

    GruYaTheme {
        GruYaApp()
    }
}*/

    /*
        AppDestinations.entries.forEach { destination ->

            item(

                icon = {

                    when (destination) {

                        AppDestinations.HOME -> {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = destination.label
                            )
                        }

                        AppDestinations.FAVORITES -> {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = destination.label
                            )
                        }

                        AppDestinations.PROFILE -> {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = destination.label
                            )
                        }
                    }
                },

                label = {
                    Text(destination.label)
                },

                selected = destination == currentDestination,

                onClick = {
                    currentDestination = destination
                }
            )
        }
    }

    ) {

        when (currentDestination) {

            AppDestinations.HOME -> {
                HomeScreen()
            }

            AppDestinations.FAVORITES -> {
                FavoriteScreen()
            }

            AppDestinations.PROFILE -> {
                ProfileScreen()
            }
        }
    }
    }
    }


    enum class AppDestinations(
        val label: String
    ) {

        HOME("Inicio"),

        FAVORITES("Favoritos"),

        PROFILE("Perfil")
    }*/

