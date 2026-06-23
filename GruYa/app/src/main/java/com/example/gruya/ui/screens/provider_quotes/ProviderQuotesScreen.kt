package com.example.gruya.ui.screens.provider_quotes

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.Quote
import com.example.gruya.domain.model.QuoteStatus
import com.example.gruya.domain.model.ServiceType
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CheckCircleOutline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderQuotesScreen(
    viewModel: ProviderQuotesViewModel = hiltViewModel(),
    initialFilter: ProviderQuoteFilter? = null,
    onNavigateToTracking: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filters = ProviderQuoteFilter.entries
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = filters.indexOf(initialFilter ?: uiState.selectedFilter).coerceAtLeast(0),
        pageCount = { filters.size }
    )

    // Sync pager with ViewModel when swiping
    LaunchedEffect(pagerState.currentPage) {
        val newFilter = filters[pagerState.currentPage]
        if (newFilter != uiState.selectedFilter) {
            viewModel.onFilterSelected(newFilter)
        }
    }

    // Sync ViewModel with pager (for external changes or initial filter)
    LaunchedEffect(uiState.selectedFilter) {
        val targetPage = filters.indexOf(uiState.selectedFilter)
        if (targetPage != pagerState.currentPage && targetPage != -1) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Cotizaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tabs Row
            FilterTabs(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { filter ->
                    scope.launch {
                        pagerState.animateScrollToPage(filters.indexOf(filter))
                    }
                }
            )

            // Filter description
            Text(
                text = uiState.selectedFilter.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                val currentFilter = filters[pageIndex]
                
                // Show content only if it matches the current selection to avoid showing old data
                // while the new data is loading for the specific filter.
                // Note: The current ViewModel architecture only holds one list.
                if (currentFilter == uiState.selectedFilter) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            uiState.isLoading && !uiState.isRefreshing -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            uiState.error != null && uiState.quotes.isEmpty() -> {
                                ProviderQuotesErrorContent(
                                    error = uiState.error!!,
                                    onRetry = { viewModel.loadQuotes() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                ProviderQuotesListContent(
                                    quotes = uiState.quotes,
                                    isRefreshing = uiState.isRefreshing,
                                    selectedFilter = currentFilter,
                                    onRefresh = viewModel::onRefresh,
                                    onCancelQuote = viewModel::onCancelQuote,
                                    onQuoteClick = { quote ->
                                        if (quote.status == QuoteStatus.ACEPTADA) {
                                            onNavigateToTracking(quote.assistance.id)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    // Loading state for other pages during swipe
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: ProviderQuoteFilter,
    onFilterSelected: (ProviderQuoteFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = ProviderQuoteFilter.entries
    val selectedIndex = filters.indexOf(selectedFilter)

    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        divider = {}
    ) {
        filters.forEachIndexed { index, filter ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderQuotesListContent(
    quotes: List<Quote>,
    isRefreshing: Boolean,
    selectedFilter: ProviderQuoteFilter,
    onRefresh: () -> Unit,
    onCancelQuote: (Int) -> Unit,
    onQuoteClick: (Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        if (quotes.isEmpty()) {
            EmptyState(filter = selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(quotes, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        onCancel = { onCancelQuote(quote.id) },
                        onClick = { onQuoteClick(quote) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyState(filter: ProviderQuoteFilter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = when (filter) {
                ProviderQuoteFilter.PENDIENTES -> Icons.Default.HourglassEmpty
                ProviderQuoteFilter.ACEPTADAS -> Icons.Default.CheckCircleOutline
                ProviderQuoteFilter.FINALIZADAS -> Icons.Default.History
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = emptyMessageFor(filter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Desliza hacia abajo para actualizar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

private fun emptyMessageFor(filter: ProviderQuoteFilter): String = when (filter) {
    ProviderQuoteFilter.ACEPTADAS -> "No tienes cotizaciones aceptadas en curso."
    ProviderQuoteFilter.PENDIENTES -> "No tienes cotizaciones pendientes de respuesta."
    ProviderQuoteFilter.FINALIZADAS -> "No tienes cotizaciones finalizadas."
}

@Composable
private fun QuoteCard(
    quote: Quote,
    onCancel: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assistance = quote.assistance
    val vehicle = assistance.vehicle

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = quote.status == QuoteStatus.ACEPTADA) { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Client + Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconForServiceType(
                                serviceType = assistance.serviceType,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = assistance.clientName.ifEmpty { "Cliente" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assistance.issueType.toDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (quote.status == QuoteStatus.PENDIENTE) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Details: Vehicle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${vehicle.brand} ${vehicle.model} • ${vehicle.color}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer: Price and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Presupuesto",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.0f", quote.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                QuoteStatusBadge(status = quote.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Date
            Text(
                text = "Enviada el ${quote.createdAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

private fun IssueType.toDisplayName(): String = name.lowercase()
    .replace("_", " ")
    .replaceFirstChar { it.uppercase() }

@Composable
private fun IconForServiceType(
    serviceType: ServiceType,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (serviceType) {
        ServiceType.AUXILIO -> Icons.Default.CarRepair
        ServiceType.GOMERIA -> Icons.Default.TireRepair
        ServiceType.MECANICO -> Icons.Default.Build
    }
    Icon(
        imageVector = icon,
        contentDescription = serviceType.displayName,
        modifier = modifier,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun QuoteStatusBadge(status: QuoteStatus) {
    val (label, badgeColor) = when (status) {
        QuoteStatus.PENDIENTE -> "Pendiente" to MaterialTheme.colorScheme.primary
        QuoteStatus.ACEPTADA -> "Aceptada" to Color(0xFF22C55E)
        QuoteStatus.RECHAZADA -> "Rechazada" to Color(0xFFEF4444)
        QuoteStatus.CANCELADA -> "Cancelada" to Color.Gray
        QuoteStatus.EXPIRADA -> "Expirada" to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeColor.copy(alpha = 0.12f),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = badgeColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ProviderQuotesErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Ups! Algo salió mal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Preview(
    name = "ProviderQuotesScreen – Light",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun ProviderQuotesScreenPreview() {
    ProviderQuotesScreen()
}
