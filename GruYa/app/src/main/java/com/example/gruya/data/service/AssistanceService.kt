package com.example.gruya.data.service

import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AssistanceService {
    @POST("/api/services/request")
    suspend fun create(@Body request: CreateAssistanceRequest): Response<Void>
}
