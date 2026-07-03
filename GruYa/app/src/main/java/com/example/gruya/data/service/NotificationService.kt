package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.response.NotificationResponse
import com.example.gruya.data.remote.dtos.response.PagedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationService {
    @GET(Constants.NOTIFICATIONS_PATH)
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<PagedResponse<NotificationResponse>>

    @PATCH("${Constants.NOTIFICATIONS_PATH}/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Int
    ): Response<NotificationResponse>

    @PATCH("${Constants.NOTIFICATIONS_PATH}/read-all")
    suspend fun markAllAsRead(): Response<Unit>
}
