package cl.duoc.level_up_mobile.data.remote.user.dto

data class UsuarioPublicRequest(
    val firebaseUid: String,
    val email: String,
    val nombre: String,
    val fechaNacimiento: String? = null,
    val avatarUrl: String? = null,
    val rol: String? = null
)
