package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateQuoteRequest
import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.data.remote.dtos.response.QuoteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QuoteService {
    @POST(Constants.QUOTES_PATH)
    suspend fun create(@Body request: CreateQuoteRequest): Response<QuoteResponse>

    @GET(Constants.QUOTES_PATH + "/mine")
    suspend fun getMine(@Query("status") statuses: List<String> = emptyList()): Response<List<QuoteResponse>>

    @GET(Constants.QUOTES_PATH + "/by-assistance/{assistanceId}")
    suspend fun getByAssistance(@Path("assistanceId") id: Int): Response<List<QuoteResponse>>

    @GET(Constants.QUOTES_PATH + "/requests-for-me")
    suspend fun getRequestsForMe(): Response<List<AssistanceResponse>>

    @PUT(Constants.QUOTES_PATH + "/{quoteId}/accept")
    suspend fun accept(@Path("quoteId") id: Int): Response<QuoteResponse>

    @PUT(Constants.QUOTES_PATH + "/{quoteId}/reject")
    suspend fun reject(@Path("quoteId") id: Int): Response<QuoteResponse>

    @PUT(Constants.QUOTES_PATH + "/{quoteId}/cancel")
    suspend fun cancel(@Path("quoteId") id: Int): Response<QuoteResponse>
}
