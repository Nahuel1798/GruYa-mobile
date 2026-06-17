package com.example.gruya.data.repository

import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreateQuoteRequest
import com.example.gruya.data.service.QuoteService
import com.example.gruya.domain.model.Quote
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

    suspend fun getByAssistance(assistanceId: Int): Result<List<Quote>> {
        return try {
            val response = quoteService.getByAssistance(assistanceId)
            if (response.isSuccessful) {
                val quotes = response.body()?.toDomain().orEmpty()
                Result.success(quotes)
            } else {
                Result.failure(Exception("No se encontraron presupuestos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al cargar presupuestos", e))
        }
    }

    suspend fun acceptQuote(quoteId: Int): Result<Unit> {
        return try {
            val response = quoteService.accept(quoteId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al aceptar el presupuesto"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al aceptar el presupuesto", e))
        }
    }

    suspend fun rejectQuote(quoteId: Int): Result<Unit> {
        return try {
            val response = quoteService.reject(quoteId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al rechazar el presupuesto"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al rechazar el presupuesto", e))
        }
    }
}
