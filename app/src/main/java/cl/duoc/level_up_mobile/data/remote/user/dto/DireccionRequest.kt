package cl.duoc.level_up_mobile.data.remote.user.dto

data class DireccionRequest(
    val alias: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val depto: String? = null,
    val ciudad: String? = null,
    val region: String? = null,
    val pais: String? = null
)
