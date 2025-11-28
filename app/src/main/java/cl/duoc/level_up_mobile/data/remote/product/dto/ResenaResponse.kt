package cl.duoc.level_up_mobile.data.remote.product.dto

data class ResenaResponse(
    val id: Long?,
    val comentario: String?,
    val puntuacion: Int?,
    val usuarioId: String?,
    val createdAt: String?,
    val usuarioNombre: String?,      // @Transient en backend, pero viene en JSON
    val usuarioAvatarUrl: String?    // igual
)