package com.example.gruya.ui.screens.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {

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