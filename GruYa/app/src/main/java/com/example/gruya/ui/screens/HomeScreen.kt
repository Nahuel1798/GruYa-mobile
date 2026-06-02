package com.example.gruya.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import coil.compose.rememberAsyncImagePainter
import com.example.gruya.ui.theme.GruYaTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


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
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // MAPA (Fondo)
            val sanLuis = LatLng(-33.2950, -66.3356)

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    sanLuis,
                    14f
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {

//                Marker(
//                    state = MarkerState(position = sanLuis),
//                    title = "Mi ubicación"
//                )
            }

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
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
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

                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,

                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Mapa",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                var startAnimation by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(Unit) {
                    startAnimation = true
                }


                // Estado para animación
                var panelVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    panelVisible = true
                }

                AnimatedVisibility(
                    visible = panelVisible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) +
                            fadeIn() +
                            scaleIn(
                                initialScale = 0.9f
                            ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight }
                    ) + fadeOut()
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 10.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
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
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(50)
                                    )
                                    .align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "¿Qué necesitas hoy?",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Selecciona un servicio para recibir asistencia inmediata.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    showDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
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
                                containerColor = MaterialTheme.colorScheme.secondary
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
                        Text(
                            "Confirmar Auxilio",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            "Estamos por enviar una unidad de emergencia a tu ubicación actual.",
                            color = MaterialTheme.colorScheme.onSurface
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
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    RoundedCornerShape(50)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}