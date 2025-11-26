package cl.duoc.level_up_mobile.data.remote.auth

import cl.duoc.level_up_mobile.data.remote.auth.dto.AuthResponse
import cl.duoc.level_up_mobile.data.remote.auth.dto.LoginRequest
import cl.duoc.level_up_mobile.data.remote.auth.dto.RefreshRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>

    @POST("/auth/refresh")
    suspend fun refresh(
        @Body body: RefreshRequest
    ): Response<AuthResponse>

    @POST("/auth/logout")
    suspend fun logout(
        @Body body: RefreshRequest
    ): Response<Unit>
}
