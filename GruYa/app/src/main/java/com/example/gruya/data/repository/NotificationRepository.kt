package com.example.gruya.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor() {
    private val _notifications = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val notifications = _notifications.asSharedFlow()

    suspend fun emitNotification(title: String, body: String) {
        _notifications.emit(title to body)
    }
}
