package cl.duoc.level_up_mobile.data.remote.core

import cl.duoc.level_up_mobile.session.AuthSession
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = AuthSession.accessToken

        return if (token.isNullOrBlank()) {
            // No hay token, seguimos tal cual
            chain.proceed(original)
        } else {
            val newRequest = original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }
    }
}
