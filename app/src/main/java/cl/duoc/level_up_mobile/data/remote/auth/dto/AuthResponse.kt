package cl.duoc.level_up_mobile.data.remote.auth.dto

data class AuthResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val userId: String?,
    val email: String?,
    val rol: String?
)
