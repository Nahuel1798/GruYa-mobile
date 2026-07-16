package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreatePaymentRequest
import com.example.gruya.data.remote.dtos.response.PaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentService {

    // Crear un pago para una asistencia
    @POST(Constants.PAYMENT_PATH + "/{assistanceId}")
    suspend fun createPayment(
        @Path("assistanceId") assistanceId: Int,
        @Body request: CreatePaymentRequest
    ): Response<PaymentResponse>

    // Obtener un pago por ID
    @GET(Constants.PAYMENT_PATH + "/{id}")
    suspend fun getPayment(
        @Path("id") id: Int
    ): Response<PaymentResponse>

    // Obtener el pago asociado a una asistencia
    @GET(Constants.PAYMENT_PATH + "/assistance/{assistanceId}")
    suspend fun getPaymentByAssistance(
        @Path("assistanceId") assistanceId: Int
    ): Response<PaymentResponse>

    // Obtener todos los pagos del usuario autenticado
    @GET(Constants.PAYMENT_PATH)
    suspend fun getMyPayments(): Response<List<PaymentResponse>>
}
