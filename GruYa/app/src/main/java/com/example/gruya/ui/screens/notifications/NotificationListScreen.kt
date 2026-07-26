package com.example.gruya.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.gruya.ui.components.ScreenScaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.data.remote.dtos.response.NotificationResponse
import com.example.gruya.utils.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNotification: (String, Int, String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hasUnread = remember(uiState.notifications) {
        uiState.notifications.any { it.readAt == null }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (uiState.notifications.isNotEmpty()) {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    ScreenScaffold(
        title = "Notificaciones",
        onBack = onNavigateBack,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (hasUnread) {
                TextButton(
                    onClick = { viewModel.markAllAsRead() },
                    enabled = !uiState.isMarkingAllAsRead
                ) {
                    if (uiState.isMarkingAllAsRead) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Marcar leídas",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
                onDeleteNotification = { notification ->
                    viewModel.deleteNotificationLocally(notification.id)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Notificación eliminada",
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteNotification()
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListContent(
    modifier: Modifier = Modifier,
    uiState: NotificationListUiState,
    onNotificationClick: (NotificationResponse) -> Unit,
    onDeleteNotification: (NotificationResponse) -> Unit,
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
        EmptyNotificationsContent(modifier = modifier)
    } else {
        val notificationsByDate = remember(uiState.notifications) {
            uiState.notifications.groupBy { notification ->
                if (DateTimeUtils.isToday(notification.sentAt)) "Hoy" else "Anteriores"
            }
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            notificationsByDate.forEach { (dateGroup, notifications) ->
                item {
                    Text(
                        text = dateGroup,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(notifications, key = { _, item -> item.id }) { _, notification ->
                    val swipeState = rememberSwipeToDismissBoxState()
                    
                    LaunchedEffect(swipeState.currentValue) {
                        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            onDeleteNotification(notification)
                            delay(300)
                            swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                        }
                    }

                    SwipeToDismissBox(
                        state = swipeState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = when (swipeState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    ) {
                        NotificationItem(
                            notification = notification,
                            onClick = { onNotificationClick(notification) }
                        )
                    }
                }
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
fun EmptyNotificationsContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Todo al día",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No tienes notificaciones por el momento. Te avisaremos cuando ocurra algo importante.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationResponse,
    onClick: () -> Unit
) {
    val isUnread = notification.readAt == null
    val typeColor = colorForNotificationType(notification.type)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 0.dp
        ),
        border = if (isUnread) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = typeColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconForNotificationType(notification.type),
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = DateTimeUtils.formatRelative(notification.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun colorForNotificationType(type: String): Color = when (type) {
    "new_assistance", "directed_assistance" -> MaterialTheme.colorScheme.primary
    "new_quote" -> MaterialTheme.colorScheme.secondary
    "quote_accepted_provider", "quote_accepted_client", "provider.service_completed" -> Color(0xFF4CAF50)
    "quote_rejected" -> MaterialTheme.colorScheme.error
    "trip_started", "provider.arrived", "provider.heading_to_destination" -> Color(0xFFFF9800)
    else -> MaterialTheme.colorScheme.outline
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
