// cl/duoc/level_up_mobile/model/Producto.kt
package cl.duoc.level_up_mobile.model

data class Producto(
    val codigo: String,
    val nombre: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val categoria: String,
    val imagenUrl: String,
    val precio: String,        // formateado para la UI
    val puntuacion: String,    // ej: "4.3"

    val especificaciones: List<String> = emptyList(),
    val stock: Int = 0,
    val comentarios: List<String> = emptyList()
)
