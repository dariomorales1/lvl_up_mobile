package cl.duoc.level_up_mobile.data.remote.user.dto

data class UsuarioResponse(
    val id: String?,
    val email: String?,
    val nombre: String?,
    val fechaNacimiento: String?,
    val avatarUrl: String?,
    val activo: Boolean?,
    val rol: String?,
    val creadoEn: String?,
    val actualizadoEn: String?
)
