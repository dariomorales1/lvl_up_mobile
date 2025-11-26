package cl.duoc.level_up_mobile.data.remote.user.dto

data class UsuarioRequest(
    val email: String,
    val nombre: String,
    val fechaNacimiento: String?,
    val avatarUrl: String?,
    val rol: String?,
    val activo: Boolean?
)
