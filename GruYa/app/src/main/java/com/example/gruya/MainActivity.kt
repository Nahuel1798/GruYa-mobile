package com.example.gruya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gruya.ui.navigation.AppDest
import com.example.gruya.ui.screens.HomeScreen
import com.example.gruya.ui.screens.auth.AuthViewModel
import com.example.gruya.ui.screens.auth.login.LoginScreen
import com.example.gruya.ui.screens.auth.register.RegisterScreen
import com.example.gruya.ui.screens.favorites.FavoritesScreen
import com.example.gruya.ui.screens.profile.ProfileScreen
import com.example.gruya.ui.theme.GruYaTheme

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
    authViewModel: AuthViewModel = viewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val backStack = rememberNavBackStack(
        if (isLoggedIn) AppDest.MainContent
        else AppDest.Login
    )

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {

            entry<AppDest.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        authViewModel.onLoginSuccess()

                        backStack.clear()
                        backStack.add(AppDest.MainContent)
                    },
                    onNavigateToRegister = {
                        backStack.add(AppDest.Register)
                    }
                )
            }

            entry<AppDest.Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        backStack.clear()
                        backStack.add(AppDest.Login)
                    }
                )
            }

            entry<AppDest.MainContent> {
                MainNavigationSuite(
                    onLogout = {
                        authViewModel.logout()

                        backStack.clear()
                        backStack.add(AppDest.Login)
                    }
                )
            }
        }
    )
}

@Composable
fun MainNavigationSuite(
    onLogout: () -> Unit
) {
    val tabBackStack = rememberNavBackStack(
        AppDest.TabKey.Home
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {

            val tabs = listOf(
                Triple(
                    AppDest.TabKey.Home,
                    "Inicio",
                    Icons.Default.Home
                ),
                Triple(
                    AppDest.TabKey.Favourites,
                    "Seguimiento",
                    Icons.Default.Add
                ),
                Triple(
                    AppDest.TabKey.Profile,
                    "Perfil",
                    Icons.Default.AccountCircle
                )
            )

            tabs.forEach { (key, label, icon) ->

                val selected =
                    tabBackStack.lastOrNull() == key

                item(
                    selected = selected,

                    onClick = {
                        if (!selected) {
                            tabBackStack.clear()
                            tabBackStack.add(key)
                        }
                    },

                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label
                        )
                    },

                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            NavDisplay(
                backStack = tabBackStack,
                entryProvider = entryProvider {

                    entry<AppDest.TabKey.Home> {
                        HomeScreen()
                    }

                    entry<AppDest.TabKey.Favourites> {
                        FavoritesScreen()
                    }

                    entry<AppDest.TabKey.Profile> {
                        ProfileScreen(
                            onLogout = onLogout
                        )
                    }
                }
            )
        }
    }
}

