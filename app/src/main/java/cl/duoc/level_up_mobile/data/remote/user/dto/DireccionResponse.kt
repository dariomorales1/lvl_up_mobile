package cl.duoc.level_up_mobile.data.remote.user.dto

data class DireccionResponse(
    val id: Long,
    val alias: String?,
    val calle: String?,
    val numero: String?,
    val depto: String?,
    val ciudad: String?,
    val region: String?,
    val pais: String?,
    val creadoEn: String?,
    val actualizadoEn: String?
)
