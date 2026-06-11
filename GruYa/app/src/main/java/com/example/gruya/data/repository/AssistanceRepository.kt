package com.example.gruya.data.repository

import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.service.AssistanceService
import javax.inject.Inject

class AssistanceRepository @Inject constructor(
    private val assistanceService: AssistanceService
) {
    suspend fun create(request: CreateAssistanceRequest): Result<Unit> {
        val response = assistanceService.create(request)
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                Exception("${response.code()}: ${response.message()}")
            )
        }
    }
}
