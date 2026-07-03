package com.example.gruya.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gruya.data.remote.dtos.response.NotificationResponse
import com.example.gruya.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationListUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val totalPages: Int = 1,
    val isMarkingAllAsRead: Boolean = false,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            notificationRepository.getNotifications(page, 20).onSuccess { pagedResponse ->
                if (pagedResponse != null) {
                    _uiState.update { 
                        it.copy(
                            notifications = if (page == 1) pagedResponse.data else it.notifications + pagedResponse.data,
                            page = pagedResponse.page,
                            totalPages = pagedResponse.totalPages,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            // Optimistic update
            val previousReadAt = _uiState.value.notifications.find { it.id == notificationId }?.readAt
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map {
                        if (it.id == notificationId) it.copy(readAt = java.time.Instant.now().toString()) else it
                    }
                )
            }
            notificationRepository.markAsRead(notificationId).onSuccess { updatedNotification ->
                if (updatedNotification != null) {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notificationId) updatedNotification else it
                            }
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        notifications = state.notifications.map {
                            if (it.id == notificationId) it.copy(readAt = previousReadAt) else it
                        },
                        error = e.message
                    )
                }
            }
        }
    }

    fun markAllAsRead() {
        if (_uiState.value.isMarkingAllAsRead) return

        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAllAsRead = true) }
            notificationRepository.markAllAsRead().onSuccess {
                _uiState.update { state ->
                    state.copy(
                        notifications = state.notifications.map { it.copy(readAt = java.time.Instant.now().toString()) },
                        isMarkingAllAsRead = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isMarkingAllAsRead = false, error = e.message) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            notificationRepository.getNotifications(1, 20).onSuccess { pagedResponse ->
                if (pagedResponse != null) {
                    _uiState.update {
                        it.copy(
                            notifications = pagedResponse.data,
                            page = pagedResponse.page,
                            totalPages = pagedResponse.totalPages,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isRefreshing = false, isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
