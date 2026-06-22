package com.example.gruya.data.remote

import com.example.gruya.BuildConfig

object Constants {
    val BASE_URL = BuildConfig.BASE_URL
//    val BASE_URL = "http:/192.168.1.37:5082/"
    const val AUTH_PATH = "/api/auth"
    const val PROVIDER_PROFILE_PATH = "/api/providerprofiles/"
    const val VEHICLES_PATH = "/api/vehicles"
    const val ASSISTANCE_PATH = "/api/assistances"
    const val QUOTES_PATH = "/api/quotes"
    const val FUELSTATION_PATH = "/api/fuelstations"
}