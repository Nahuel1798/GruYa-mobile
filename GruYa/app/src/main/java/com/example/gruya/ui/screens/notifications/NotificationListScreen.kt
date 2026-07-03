package com.example.gruya.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.data.remote.dtos.response.NotificationResponse
import com.example.gruya.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNotification: (String, Int, String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Only show snackbar for errors when there are already notifications visible
            // For initial load errors, the inline error screen handles it
            if (uiState.notifications.isNotEmpty()) {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllAsRead() },
                        enabled = !uiState.isMarkingAllAsRead
                    ) {
                        if (uiState.isMarkingAllAsRead) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Marcar todo como leído")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding)
        ) {
            NotificationListContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onNotificationClick = { notification ->
                    viewModel.markAsRead(notification.id)
                    notification.assistanceId?.let { assistanceId ->
                        onNavigateToNotification(notification.type, assistanceId, notification.dataJson)
                    }
                },
                onLoadMore = {
                    if (uiState.page < uiState.totalPages) {
                        viewModel.loadNotifications(uiState.page + 1)
                    }
                },
                onRetry = { viewModel.loadNotifications() }
            )
        }
    }
}

@Composable
fun NotificationListContent(
    modifier: Modifier = Modifier,
    uiState: NotificationListUiState,
    onNotificationClick: (NotificationResponse) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit = {}
) {
    if (uiState.error != null && uiState.notifications.isEmpty() && !uiState.isLoading) {
        NotificationErrorContent(
            modifier = modifier,
            error = uiState.error!!,
            onRetry = onRetry
        )
    } else if (uiState.notifications.isEmpty() && !uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No tienes notificaciones por el momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
            }
            
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            } else if (uiState.page < uiState.totalPages) {
                item {
                    TextButton(
                        onClick = onLoadMore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cargar más")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationResponse,
    onClick: () -> Unit
) {
    val isUnread = notification.readAt == null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForNotificationType(notification.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Text(
                    text = DateTimeUtils.formatRelative(notification.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.Top)
                )
            }
        }
    }
}

private fun iconForNotificationType(type: String): ImageVector = when (type) {
    "new_assistance", "directed_assistance" -> Icons.Default.LocalShipping
    "new_quote" -> Icons.Default.Info
    "quote_accepted_provider", "quote_accepted_client" -> Icons.Default.CheckCircle
    "quote_rejected" -> Icons.Default.Cancel
    "trip_started", "provider.arrived", "provider.heading_to_destination" -> Icons.Default.DirectionsCar
    "provider.service_completed" -> Icons.Default.CheckCircle
    else -> Icons.Default.NotificationsNone
}

@Composable
private fun NotificationErrorContent(
    modifier: Modifier = Modifier,
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
