package com.example.gruya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.gruya.network.ApiClient
import com.example.gruya.network.dtos.request.LoginRequest
import com.example.gruya.ui.screens.HomeScreen
import com.example.gruya.ui.screens.LoginScreen
import com.example.gruya.ui.theme.GruYaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

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
fun GruYaApp() {
    val scope = rememberCoroutineScope()

    var isLogged by rememberSaveable {
        mutableStateOf(false)
    }

    if (!isLogged) {

        LoginScreen(

            onLoginClick = { email, password ->

                scope.launch {

                    try {

                        val request = LoginRequest(email, password)
                        val apiClient = ApiClient.api

                        apiClient.login(request)

                        isLogged = true

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )

    } else {

        var currentDestination by rememberSaveable {
            mutableStateOf(AppDestinations.HOME)
        }

        NavigationSuiteScaffold(

            navigationSuiteItems = {

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen() {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "Servicios Favoritos"
                    )
                }
            )
        }

    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            color = Color(0xFFF9F9FF)
        ) {

            Text(
                text = "Todavía no tienes servicios guardados."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Mi Perfil")
                }
            )
        }

    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            color = Color(0xFFF9F9FF)
        ) {

            Text(
                text = "Información del usuario."
            )
        }
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
fun GruYaAppPreview() {

    GruYaTheme {
        GruYaApp()
    }
}