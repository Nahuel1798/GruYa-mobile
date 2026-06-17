package com.example.gruya.data.repository

import com.example.gruya.data.remote.dtos.request.CreateQuoteRequest
import com.example.gruya.data.service.QuoteService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    private val quoteService: QuoteService
) {
    suspend fun sendQuote(assistanceId: Int, price: Double): Result<Unit> {
        return try {
            val response = quoteService.create(CreateQuoteRequest(assistanceId, price))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Datos inválidos para el presupuesto"
                    404 -> "La solicitud de asistencia no existe"
                    409 -> "Ya existe un presupuesto para esta solicitud"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}", e))
        }
    }
}
