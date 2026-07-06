package com.example.gruya.data.service

import com.example.gruya.data.remote.dtos.request.CreatePaymentRequest
import com.example.gruya.data.remote.dtos.response.PaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentService {
    @POST("payment/{assistanceId}")
    suspend fun createPayment(
        @Path("assistanceId") assistanceId: Int,
        @Body request: CreatePaymentRequest
    ): Response<PaymentResponse>

    @GET("payment/{id}")
    suspend fun getPayment(
        @Path("id") id: Int
    ): Response<PaymentResponse>

    @GET("payment/assistance/{assistanceId}")
    suspend fun getPaymentByAssistance(
        @Path("assistanceId") assistanceId: Int
    ): Response<PaymentResponse>

    @GET("payment")
    suspend fun getMyPayments(): Response<List<PaymentResponse>>
}
