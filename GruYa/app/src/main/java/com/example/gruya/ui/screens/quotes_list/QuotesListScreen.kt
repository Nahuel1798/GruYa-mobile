package com.example.gruya.ui.screens.quotes_list

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.domain.model.Assistance
import com.example.gruya.domain.model.Quote
import com.example.gruya.domain.model.QuoteStatus
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesListScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuotesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val acceptedQuote = remember(uiState.quotes) {
        uiState.quotes.find { it.status == QuoteStatus.ACEPTADA }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (acceptedQuote != null) "Servicio en curso" else "Respuestas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (acceptedQuote != null) Color.Transparent else MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.quotes.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.quotes.isEmpty() -> {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.quotes.isEmpty() -> {
                    Text(
                        text = "No hay presupuestos disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // Background Map
                    val firstAssistance = uiState.quotes.firstOrNull()?.assistance
                    if (firstAssistance != null) {
                        QuotesListMap(
                            assistance = acceptedQuote?.assistance ?: firstAssistance,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (acceptedQuote == null) {
                        // Full screen list hiding the map
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            QuotesListContent(
                                quotes = uiState.quotes,
                                actionLoading = uiState.actionLoading,
                                onAccept = viewModel::accept,
                                onReject = viewModel::reject
                            )
                        }
                    } else {
                        // Active Quote Card at the bottom
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            QuoteCard(
                                quote = acceptedQuote,
                                actionLoading = uiState.actionLoading,
                                onAccept = {},
                                onReject = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotesListMap(
    assistance: Assistance,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(assistance.origin.longitude, assistance.origin.latitude),
            zoom = 13.0
        )
    )

    LaunchedEffect(assistance) {
        val target = if (assistance.destination.latitude != 0.0) {
            Position(
                (assistance.origin.longitude + assistance.destination.longitude) / 2.0,
                (assistance.origin.latitude + assistance.destination.latitude) / 2.0
            )
        } else {
            Position(assistance.origin.longitude, assistance.origin.latitude)
        }

        cameraState.animateTo(
            CameraPosition(
                target = target,
                zoom = 12.0
            )
        )
    }

    MaplibreMap(
        modifier = modifier,
        cameraState = cameraState,
        baseStyle = BaseStyle.Uri(
            if (isDark) "https://tiles.openfreemap.org/styles/dark" else "https://tiles.openfreemap.org/styles/liberty"
        )
    ) {
        // Route
        assistance.routeGeometry?.let { geometry ->
            val routePositions = remember(geometry) {
                parseRouteGeometry(geometry)
            }

            if (routePositions.isNotEmpty()) {
                val routeSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(
                                        coordinates = routePositions
                                    ),
                                    properties = null
                                )
                            )
                        )
                    )
                )

                LineLayer(
                    id = "assistance-route",
                    source = routeSource,
                    color = const(MaterialTheme.colorScheme.primary),
                    width = const(5.dp)
                )
            }
        }

        val features = remember(assistance) {
            buildList {
                add(
                    Feature(
                        geometry = Point(
                            longitude = assistance.origin.longitude,
                            latitude = assistance.origin.latitude
                        ),
                        properties = null
                    )
                )

                if (assistance.destination.latitude != 0.0) {
                    add(
                        Feature(
                            geometry = Point(
                                longitude = assistance.destination.longitude,
                                latitude = assistance.destination.latitude
                            ),
                            properties = null
                        )
                    )
                }
            }
        }

        val markersSource = rememberGeoJsonSource(
            data = GeoJsonData.Features(
                geoJson = FeatureCollection(features = features)
            )
        )

        CircleLayer(
            id = "assistance-points",
            source = markersSource,
            color = const(MaterialTheme.colorScheme.primary),
            radius = const(10.dp),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp)
        )
    }
}

@Composable
private fun QuotesListContent(
    quotes: List<Quote>,
    actionLoading: Int?,
    onAccept: (Int) -> Unit,
    onReject: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(quotes, key = { it.id }) { quote ->
            QuoteCard(
                quote = quote,
                actionLoading = actionLoading,
                onAccept = onAccept,
                onReject = onReject
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    actionLoading: Int?,
    onAccept: (Int) -> Unit,
    onReject: (Int) -> Unit
) {
    val isActionLoading = actionLoading == quote.id
    val priceFormatted = "$%.2f".format(quote.price)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                QuoteStatusBadge(status = quote.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Provider name
            Text(
                text = quote.providerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            // Created at
            Text(
                text = quote.createdAt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Accept / Reject buttons (only for PENDIENTE)
            if (quote.status == QuoteStatus.PENDIENTE) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReject(quote.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isActionLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Rechazar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onAccept(quote.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isActionLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Aceptar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteStatusBadge(status: QuoteStatus) {
    val (label, badgeColor) = when (status) {
        QuoteStatus.PENDIENTE -> "Pendiente" to MaterialTheme.colorScheme.primary
        QuoteStatus.ACEPTADA -> "Aceptada" to Color(0xFF22C55E)
        QuoteStatus.RECHAZADA -> "Rechazada" to MaterialTheme.colorScheme.error
        QuoteStatus.CANCELADA -> "Cancelada" to MaterialTheme.colorScheme.onSurfaceVariant
        QuoteStatus.EXPIRADA -> "Expirada" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = badgeColor.copy(alpha = 0.15f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = badgeColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun parseRouteGeometry(routeGeometry: String): List<Position> {
    return try {
        val json = JSONArray(routeGeometry)
        buildList {
            for (i in 0 until json.length()) {
                val coord = json.getJSONArray(i)
                add(
                    Position(
                        longitude = coord.getDouble(0),
                        latitude = coord.getDouble(1)
                    )
                )
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}
