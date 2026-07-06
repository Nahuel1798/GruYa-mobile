package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreatePaymentRequest
import com.example.gruya.data.service.PaymentService
import com.example.gruya.domain.model.Payment
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val paymentService: PaymentService
) {
    suspend fun createPayment(assistanceId: Int, request: CreatePaymentRequest): Payment? {
        return try {
            val response = paymentService.createPayment(assistanceId, request)
            if (response.isSuccessful) {
                response.body()?.toDomain()
            } else {
                Log.e("PaymentRepository", "Error creating payment: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Exception creating payment", e)
            null
        }
    }

    suspend fun getPayment(id: Int): Payment? {
        return try {
            val response = paymentService.getPayment(id)
            if (response.isSuccessful) {
                response.body()?.toDomain()
            } else {
                Log.e("PaymentRepository", "Error getting payment: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Exception getting payment", e)
            null
        }
    }

    suspend fun getPaymentByAssistance(assistanceId: Int): Payment? {
        return try {
            val response = paymentService.getPaymentByAssistance(assistanceId)
            if (response.isSuccessful) {
                response.body()?.toDomain()
            } else {
                Log.e("PaymentRepository", "Error getting payment by assistance: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Exception getting payment by assistance", e)
            null
        }
    }

    suspend fun getMyPayments(): List<Payment> {
        return try {
            val response = paymentService.getMyPayments()
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: emptyList()
            } else {
                Log.e("PaymentRepository", "Error getting my payments: ${response.code()} ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Exception getting my payments", e)
            emptyList()
        }
    }
}
