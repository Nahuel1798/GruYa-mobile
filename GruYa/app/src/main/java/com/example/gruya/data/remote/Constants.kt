package com.example.gruya.data.remote

import com.example.gruya.BuildConfig

object Constants {
    val BASE_URL = BuildConfig.BASE_URL
//    val BASE_URL = "http:/192.168.1.37:5082/"
    const val AUTH_PATH = "/api/auth"
    const val PROVIDER_PROFILE_PATH = "api/ProviderProfiles"
    const val VEHICLES_PATH = "/api/vehicles"
    const val SERVICE_PATH = "/api/services"
}