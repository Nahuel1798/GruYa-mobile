package com.example.gruya.ui.screens.provider_quotes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TimerOff
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tabs Row
            FilterTabs(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { filter ->
                    scope.launch {
                        pagerState.animateScrollToPage(filters.indexOf(filter))
                    }
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val currentFilter = filters[pageIndex]
                
                AnimatedContent(
                    targetState = uiState.isLoading && !uiState.isRefreshing && currentFilter == uiState.selectedFilter,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "ContentTransition"
                ) { loading ->
                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (currentFilter == uiState.selectedFilter) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (uiState.error != null && uiState.quotes.isEmpty()) {
                                ProviderQuotesErrorContent(
                                    error = uiState.error!!,
                                    onRetry = { viewModel.loadQuotes() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
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
                    } else {
                        // Keep content visible or show loading for other pages
                        Box(modifier = Modifier.fillMaxSize())
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
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    ) {
        filters.forEachIndexed { index, filter ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(quotes, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        onCancel = { onCancelQuote(quote.id) },
                        onClick = { onQuoteClick(quote) }
                    )
                }
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
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (filter) {
                        ProviderQuoteFilter.PENDIENTES -> Icons.Default.HourglassEmpty
                        ProviderQuoteFilter.ACEPTADAS -> Icons.Default.CheckCircleOutline
                        ProviderQuoteFilter.FINALIZADAS -> Icons.Default.History
                    },
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = emptyMessageFor(filter),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No encontramos registros en esta categoría.\nDesliza hacia abajo para actualizar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
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
    val isAccepted = quote.status == QuoteStatus.ACEPTADA
    val isInactive = quote.status in listOf(QuoteStatus.COMPLETADO, QuoteStatus.RECHAZADA, QuoteStatus.CANCELADA, QuoteStatus.EXPIRADA)
    val statusColor = getStatusColor(quote.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isAccepted || quote.status == QuoteStatus.PENDIENTE) { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isAccepted) 6.dp else 2.dp,
            pressedElevation = 8.dp
        ),
        border = if (isAccepted) {
            BorderStroke(
                width = 2.dp,
                color = statusColor.copy(alpha = 0.5f)
            )
        } else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            // Status Indicator Strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor.copy(alpha = 0.8f))
            )
            
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .alpha(if (isInactive) 0.6f else 1f)
            ) {
                // Header: Service Icon + Client + Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                IconForServiceType(
                                    serviceType = assistance.serviceType,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = assistance.clientName.ifEmpty { "Cliente GruYa" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = assistance.issueType.toDisplayName(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    if (quote.status == QuoteStatus.PENDIENTE) {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (isAccepted) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(top = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Vehicle Details
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${vehicle.brand} ${vehicle.model} • ${vehicle.color}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Price and Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRESUPUESTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%,.0f", quote.price)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAccepted) statusColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    QuoteStatusBadge(status = quote.status)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Date
                Text(
                    text = "Enviada: ${quote.createdAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getStatusColor(status: QuoteStatus): Color = when (status) {
    QuoteStatus.PENDIENTE -> Color(0xFFF59E0B) // Amber
    QuoteStatus.ACEPTADA -> Color(0xFF10B981) // Emerald
    QuoteStatus.COMPLETADO -> Color(0xFF3B82F6) // Blue
    QuoteStatus.RECHAZADA -> Color(0xFFEF4444) // Red
    QuoteStatus.CANCELADA -> Color(0xFF6B7280) // Gray
    QuoteStatus.EXPIRADA -> Color(0xFF9CA3AF) // Silver/Gray
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
    val (label, badgeColor, icon) = when (status) {
        QuoteStatus.PENDIENTE -> Triple("Pendiente", Color(0xFFF59E0B), Icons.Default.HourglassEmpty)
        QuoteStatus.ACEPTADA -> Triple("Aceptada", Color(0xFF10B981), Icons.Default.CheckCircleOutline)
        QuoteStatus.COMPLETADO -> Triple("Completada", Color(0xFF3B82F6), Icons.Default.CheckCircle)
        QuoteStatus.RECHAZADA -> Triple("Rechazada", Color(0xFFEF4444), Icons.Default.Close)
        QuoteStatus.CANCELADA -> Triple("Cancelada", Color(0xFF6B7280), Icons.Default.Cancel)
        QuoteStatus.EXPIRADA -> Triple("Expirada", Color(0xFF9CA3AF), Icons.Default.TimerOff)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = badgeColor
            )
            Text(
                text = label.uppercase(),
                color = badgeColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
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
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "¡Ups! Algo salió mal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
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
