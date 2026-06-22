package com.example.gruya.di

import com.example.gruya.BuildConfig
import com.example.gruya.AuthEventBus
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.AuthInterceptor
import com.example.gruya.data.remote.AuthResponseInterceptor
import com.example.gruya.data.service.AssistanceService
import com.example.gruya.data.service.AuthService
import com.example.gruya.data.service.FuelStationService
import com.example.gruya.data.service.ProviderService
import com.example.gruya.data.service.QuoteService
import com.example.gruya.data.service.VehicleService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): Interceptor =
        AuthInterceptor(sessionManager)

    @Provides @Singleton
    fun provideAuthResponseInterceptor(
        sessionManager: SessionManager,
        authEventBus: AuthEventBus
    ): AuthResponseInterceptor =
        AuthResponseInterceptor(sessionManager, authEventBus)

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        authResponseInterceptor: AuthResponseInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addNetworkInterceptor(authResponseInterceptor)
            .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides @Singleton
    fun provideVehicleService(retrofit: Retrofit): VehicleService =
        retrofit.create(VehicleService::class.java)

    @Provides @Singleton
    fun provideProviderService(retrofit: Retrofit): ProviderService =
        retrofit.create(ProviderService::class.java)

    @Provides @Singleton
    fun provideAssistanceService(retrofit: Retrofit): AssistanceService =
        retrofit.create(AssistanceService::class.java)

    @Provides @Singleton
    fun provideQuoteService(retrofit: Retrofit): QuoteService =
        retrofit.create(QuoteService::class.java)

    @Provides @Singleton
    fun fuelStationService(retrofit: Retrofit): FuelStationService =
        retrofit.create(FuelStationService::class.java)
}
