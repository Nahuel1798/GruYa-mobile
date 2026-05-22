package com.example.gruya.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.gruya.ui.theme.GruYaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GruYa",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF9F9FF)
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // MAPA (Fondo)
            Image(
                painter = rememberAsyncImagePainter(
                    "https://lh3.googleusercontent.com/aida-public/AB6AXuBYlUjKISAZxn-TxkaXyBFQLKlDQJ7Kfl2sUKgFRbxIVvQ4fD2j1cUUNtE3mvrIGjNe_PGbkMn2riGuniLx5qS3FRK7OoxeicCudzvjef44vUCIrUIMoWRvqtkrJSnCFtwMeuk5E_18vyKWPChM8rw4MBhP67-aocM1pDeup1h87thSOXIA2ggAQftuL599fBvzHBzpmBzkOHG1csAFFdKqA4I_f6HsZB5R92lfqvE7rBPvuuBVQ3HRErXTXzxVErPLi26HGjo4Fm3s"
                ),
                contentDescription = "Mapa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Padding del TopAppBar
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                // SEARCH BAR
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = "",
                            onValueChange = {},
                            placeholder = {
                                Text("¿A dónde necesitas ayuda?")
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Text(
                            text = "Mapa",
                            color = Color(0xFF003D9B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // PANEL INFERIOR
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(5.dp)
                                .background(
                                    Color.LightGray,
                                    RoundedCornerShape(50)
                                )
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "¿Qué necesitas hoy?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Selecciona un servicio para recibir asistencia inmediata.",
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // BOTON AUXILIO
                        Button(
                            onClick = {
                                showDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Solicitar Auxilio",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // BOTON MECANICO
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Servicios Mecánicos")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // CHIPS
                        Row(
                            modifier = Modifier.horizontalScroll(
                                rememberScrollState()
                            )
                        ) {
                            ServiceChip("Batería")
                            ServiceChip("Neumático")
                            ServiceChip("Combustible")
                            ServiceChip("Cerrajero")
                        }
                    }
                }
            }
        }

        // DIALOGO
        AnimatedVisibility(
            visible = showDialog
        ) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Confirmar y Pedir Grúa")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                },
                title = {
                    Text("Confirmar Auxilio")
                },
                text = {
                    Text(
                        "Estamos por enviar una unidad de emergencia a tu ubicación actual."
                    )
                }
            )
        }
    }
}

@Composable
fun ServiceChip(
    text: String
) {

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE8EDFF))
            .border(
                1.dp,
                Color.LightGray,
                RoundedCornerShape(50)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )
    ) {

        Text(
            text = text,
            color = Color.DarkGray
        )
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GruYaTheme {
        HomeScreen()
    }
}