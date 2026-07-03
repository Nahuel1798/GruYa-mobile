package com.example.gruya.data.repository

import com.example.gruya.data.remote.dtos.response.NotificationResponse
import com.example.gruya.data.remote.dtos.response.PagedResponse
import com.example.gruya.data.service.NotificationService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationService: NotificationService
) {
    private val _notifications = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val notifications = _notifications.asSharedFlow()

    suspend fun emitNotification(title: String, body: String) {
        _notifications.emit(title to body)
    }

    suspend fun getNotifications(page: Int, pageSize: Int): Result<PagedResponse<NotificationResponse>?> {
        return try {
            val response = notificationService.getNotifications(page, pageSize)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: Int): Result<NotificationResponse?> {
        return try {
            val response = notificationService.markAsRead(id)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = notificationService.markAllAsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
