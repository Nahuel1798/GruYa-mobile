package com.example.gruya.data.remote

import com.example.gruya.AuthEvent
import com.example.gruya.AuthEventBus
import com.example.gruya.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthResponseInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val authEventBus: AuthEventBus
) : Interceptor {
    private val isLoggedOut = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 && sessionManager.getJwt().isNotBlank()) {
            if (isLoggedOut.compareAndSet(false, true)) {
                authEventBus.emit(AuthEvent.ForceLogout)
            }
        }
        return response
    }

    fun resetLoggedOutFlag() {
        isLoggedOut.set(false)
    }
}
