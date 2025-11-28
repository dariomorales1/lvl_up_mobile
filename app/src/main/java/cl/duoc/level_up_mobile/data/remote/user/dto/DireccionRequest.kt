package cl.duoc.level_up_mobile.data.remote.user.dto

data class DireccionRequest(
    val alias: String?,
    val calle: String?,
    val numero: String?,
    val depto: String?,
    val ciudad: String?,
    val region: String?,
    val pais: String?
)