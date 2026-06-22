package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreateAssistanceRequest
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.data.remote.dtos.response.NearbyAssistanceResponse
import com.example.gruya.data.remote.dtos.response.ProviderLocationResponse
import com.example.gruya.data.service.AssistanceService
import com.example.gruya.domain.model.Assistance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistanceRepository @Inject constructor(
    private val assistanceService: AssistanceService
) {
    suspend fun create(request: CreateAssistanceRequest): Result<AssistanceResponse?> {
        return try {
            val response = assistanceService.create(request)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Solicitud de auxilio creada: $body")
                Result.success(body)
            } else {
                Result.failure(
                    Exception("${response.code()}: ${response.message()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProviderlocation(
        latitude: Double,
        longitude: Double,
    ): List<ProviderLocationResponse> {
        return assistanceService.getLocation(
            latitude = latitude,
            longitude = longitude,
        )
    }

    suspend fun getNearbyAssistances(
        rangeKm: Double = 20.0
    ): List<NearbyAssistanceResponse> {
        val result = assistanceService.getNearbyAssistances(rangeKm)
        Log.d("AssistanceRepository", "Solicitudes de auxilio cercanas: $result")
        return result
    }
    
    suspend fun getAssistanceDetails(id: Int): Result<AssistanceResponse?> {
        return try {
            val response = assistanceService.getDetailsAssistances(id)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Solicitud de auxilio creada: $body")
                Result.success(body)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserAssistances(): Result<List<Assistance>> {
        return try {
            val response = assistanceService.getUserAssistances()
            val domain = response.toDomain()
            Log.d("AssistanceRepository", "Solicitudes de auxilio del usuario: $domain")
            Result.success(domain)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener las solicitudes de auxilio", e))
        }
    }


    suspend fun getAssistanceActive(): Result<AssistanceResponse?> {
        return try {
            val response = assistanceService.getActiveAssistances()
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AssistanceRepository", "Solicitud de auxilio activa: $body")
                Result.success(body)
            } else {
                Result.failure(
                    Exception("Error ${response.code()}: ${response.message()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener la solicitud activa", e))
        }
    }
}
