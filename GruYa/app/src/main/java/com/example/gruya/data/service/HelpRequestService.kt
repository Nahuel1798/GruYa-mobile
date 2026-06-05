package com.example.gruya.data.service

import com.example.gruya.data.remote.Constants
import com.example.gruya.data.remote.dtos.request.CreateHelpRequest
import com.example.gruya.data.remote.dtos.response.HelpRequestResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface HelpRequestService {
    // Crear HelpRequest
    //@POST(Constants.AUTH_PATH + "/service/request")
    //suspend fun helprequest(@Body requestService: HelpRequestService) : HelpRequestResponse
}