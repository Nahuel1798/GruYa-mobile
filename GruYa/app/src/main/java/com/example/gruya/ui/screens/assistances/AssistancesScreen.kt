package com.example.gruya.ui.screens.assistances

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.data.local.entity.PendingAssistanceEntity
import com.example.gruya.data.local.entity.SyncStatus
import com.example.gruya.domain.model.Assistance
import com.example.gruya.domain.model.AssistanceStatus
import com.example.gruya.domain.model.IssueType
import com.example.gruya.domain.model.ServiceType
import com.example.gruya.domain.model.displayName
import com.example.gruya.ui.screens.common.PendingStatusBadge
import com.example.gruya.ui.screens.common.issueTypeIcon
import com.example.gruya.ui.screens.common.pendingStatusLabel
import com.example.gruya.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistancesScreen(
    onNavigateToQuotes: (Int) -> Unit = {},
    viewModel: AssistancesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (uiState.assistances.isNotEmpty() || uiState.activeAssistance != null) {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.loadAssistances()
        onPauseOrDispose { }
    }

    AssistancesScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::loadAssistances,
        onNavigateToQuotes = onNavigateToQuotes,
        onCancelAssistance = viewModel::cancelActiveAssistance,
        onDeletePendingAssistance = viewModel::deletePendingAssistance,
        onRetryPendingAssistance = viewModel::retryPendingAssistance
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssistancesScreenContent(
    uiState: AssistancesUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToQuotes: (Int) -> Unit,
    onCancelAssistance: () -> Unit,
    onDeletePendingAssistance: (Long) -> Unit = {},
    onRetryPendingAssistance: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mis Solicitudes",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if ((uiState.isLoading || uiState.isPerformingAction) && (uiState.assistances.isNotEmpty() || uiState.activeAssistance != null)) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "AssistancesContentTransition"
            ) { state ->
                when {
                    state.showInitialLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    state.error != null && state.assistances.isEmpty() && state.activeAssistance == null -> {
                        AssistancesErrorContent(
                            error = state.error,
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    state.assistances.isEmpty() && state.activeAssistance == null -> {
                        AssistancesEmptyContent(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        AssistancesListContent(
                            activeAssistance = state.activeAssistance,
                            assistances = state.assistances,
                            pendingLocalRequests = state.pendingLocalRequests,
                            isPerformingAction = state.isPerformingAction,
                            onNavigateToQuotes = onNavigateToQuotes,
                            onCancelAssistance = onCancelAssistance,
                            onDeletePendingAssistance = onDeletePendingAssistance,
                            onRetryPendingAssistance = onRetryPendingAssistance,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssistancesListContent(
    activeAssistance: Assistance?,
    assistances: List<Assistance>,
    pendingLocalRequests: List<PendingAssistanceEntity>,
    isPerformingAction: Boolean,
    onNavigateToQuotes: (Int) -> Unit,
    onCancelAssistance: () -> Unit,
    onDeletePendingAssistance: (Long) -> Unit = {},
    onRetryPendingAssistance: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val historyAssistances = remember(assistances, activeAssistance) {
        assistances.filter { it.id != activeAssistance?.id }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ── Pending local requests section (at the top for visibility) ──
        if (pendingLocalRequests.isNotEmpty()) {
            item(key = "pending_section_title") {
                Text(
                    text = "Pendientes sin conexión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            items(pendingLocalRequests, key = { "pending_${it.id}" }) { pending ->
                PendingLocalRequestCard(
                    pending = pending,
                    onDelete = { onDeletePendingAssistance(pending.id) },
                    onRetry = onRetryPendingAssistance,
                    isPerformingAction = isPerformingAction,
                    modifier = Modifier.animateItem()
                )
            }
        }

        activeAssistance?.let {
            item(key = "active_section_title") {
                Text(
                    text = "Solicitud Activa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 4.dp)
                )
            }
            item(key = "active_assistance_card") {
                ActiveAssistanceCard(
                    assistance = it,
                    isPerformingAction = isPerformingAction,
                    onNavigateToQuotes = onNavigateToQuotes,
                    onCancelAssistance = onCancelAssistance
                )
            }
        } ?: item(key = "empty_active") {
            EmptySectionCard(
                text = "No tienes solicitudes activas en este momento.",
                icon = Icons.Default.AccessTime
            )
        }

        item(key = "history_section_title") {
            Text(
                text = "Historial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (historyAssistances.isNotEmpty()) {
            items(historyAssistances, key = { it.id }) { assistance ->
                AssistanceCard(
                    assistance = assistance,
                    onNavigateToQuotes = onNavigateToQuotes,
                    modifier = Modifier.animateItem()
                )
            }
        } else {
            item(key = "empty_history") {
                EmptySectionCard(
                    text = "Tu historial de asistencias aparecerá aquí.",
                    icon = Icons.Default.CalendarToday
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ActiveAssistanceCard(
    assistance: Assistance,
    isPerformingAction: Boolean,
    onNavigateToQuotes: (Int) -> Unit,
    onCancelAssistance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        IconForServiceType(
                            serviceType = assistance.serviceType,
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assistance.issueType.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${assistance.vehicle.brand} ${assistance.vehicle.model} • ${assistance.vehicle.licensePlate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (assistance.createdAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DateTimeUtils.formatRelative(assistance.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = assistance.status)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SOLICITUD ACTIVA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelAssistance,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPerformingAction,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onNavigateToQuotes(assistance.id) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Seguimiento",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistanceCard(
    assistance: Assistance,
    onNavigateToQuotes: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onNavigateToQuotes(assistance.id) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconForServiceType(
                                serviceType = assistance.serviceType,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = assistance.issueType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${assistance.vehicle.brand} • ${assistance.vehicle.licensePlate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = assistance.status)
            }

            if (assistance.createdAt != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.alpha(0.1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateTimeUtils.formatIsoToDisplay(assistance.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySectionCard(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PendingLocalRequestCard(
    pending: PendingAssistanceEntity,
    onDelete: () -> Unit,
    onRetry: (Long) -> Unit = {},
    isPerformingAction: Boolean,
    modifier: Modifier = Modifier
) {
    val issueType = remember(pending.issueType) {
        try { IssueType.valueOf(pending.issueType) } catch (_: Exception) { null }
    }
    val syncStatus = remember(pending.status) {
        try { SyncStatus.valueOf(pending.status) } catch (_: Exception) { null }
    }
    val statusInfo = remember(syncStatus) { pendingStatusLabel(syncStatus) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = issueTypeIcon(issueType),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issueType?.displayName ?: "Solicitud de auxilio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateTimeUtils.formatRelative(pending.capturedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                PendingStatusBadge(syncStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusInfo.second,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusInfo.first,
                    modifier = Modifier.weight(1f)
                )
                if (!isPerformingAction) {
                    val showRetry = syncStatus == SyncStatus.FAILED || syncStatus == SyncStatus.NEEDS_REAUTH
                    if (showRetry) {
                        OutlinedButton(
                            onClick = { onRetry(pending.id) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reintentar",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Eliminar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconForServiceType(
    serviceType: ServiceType,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
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
        tint = tint
    )
}

@Composable
private fun StatusBadge(status: AssistanceStatus) {
    val (label, badgeColor) = when (status) {
        AssistanceStatus.PENDIENTE -> "Pendiente" to Color(0xFFF59E0B)
        AssistanceStatus.ACEPTADA -> "Aceptada" to Color(0xFF8B5CF6)
        AssistanceStatus.EN_CAMINO_AL_CLIENTE -> "En Camino" to Color(0xFF3B82F6)
        AssistanceStatus.EN_ORIGEN -> "En Origen" to Color(0xFFF97316)
        AssistanceStatus.EN_CAMINO_AL_DESTINO -> "A Destino" to Color(0xFF14B8A6)
        AssistanceStatus.COMPLETADO -> "Completada" to Color(0xFF10B981)
        AssistanceStatus.CANCELADO -> "Cancelada" to Color(0xFFEF4444)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.2f))
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = badgeColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun AssistancesEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(40.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CarRepair,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Sin solicitudes",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cuando solicites asistencia, aparecerá aquí para que puedas hacerle seguimiento.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun AssistancesErrorContent(
    modifier: Modifier = Modifier,
    error: String,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ups! Algo salió mal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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


@Preview(
    name = "AssistancesScreen – Light",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun AssistancesScreenPreview() {
    MaterialTheme {
        AssistancesScreenContent(
            uiState = AssistancesUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onRetry = {},
            onNavigateToQuotes = {},
            onCancelAssistance = {}
        )
    }
}
