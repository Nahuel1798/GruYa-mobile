package com.example.gruya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.gruya.ui.screens.HomeScreen
import com.example.gruya.ui.screens.LoginScreen
import com.example.gruya.ui.theme.GruYaTheme

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

@PreviewScreenSizes
@Composable
fun GruYaApp() {

    var isLogged by rememberSaveable {
        mutableStateOf(false)
    }

    if (!isLogged) {
        LoginScreen(
            onLoginClick = {email, password ->
                // Login temporal
                if(
                    email == "admin@gmail.com" &&
                    password == "1234"
                ) {
                    isLogged = true
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
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = destination.label
                            )
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

            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),

                    contentAlignment = Alignment.Center
                ) {

                    when(currentDestination) {
                        AppDestinations.HOME -> HomeScreen()

                        AppDestinations.FAVORITES -> FavoriteScreen()

                        AppDestinations.PROFILE -> ProfileScreen()
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int
) {

    HOME(
        "Home",
        R.drawable.ic_home
    ),

    FAVORITES(
        "Favorites",
        R.drawable.ic_favorite
    ),

    PROFILE(
        "Profile",
        R.drawable.ic_account_box
    )
}

@Preview(showBackground = true)
@Composable
fun GruYaAppPreview() {

    GruYaTheme {
        GruYaApp()
    }
}

@Composable
fun FavoriteScreen() { }

@Composable
fun ProfileScreen() { }