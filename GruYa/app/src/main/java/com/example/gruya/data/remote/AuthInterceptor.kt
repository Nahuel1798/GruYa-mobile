package com.example.gruya.data.remote

import com.example.gruya.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getJwt()
        val request = chain.request()

        val requestBuilder = request.newBuilder()
        
        // Solo agregar el token si es una petición a la API (contiene /api/)
        // Esto evita enviar el token a servicios externos o para imágenes estáticas
        val isApiRequest = request.url.encodedPath.contains("/api/")
        
        if (isApiRequest && !token.isNullOrBlank()){
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
