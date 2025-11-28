package cl.duoc.level_up_mobile.model

import androidx.annotation.DrawableRes
import cl.duoc.level_up_mobile.R
import com.google.gson.annotations.SerializedName

data class Producto(
    val codigo: String,
    val nombre: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val categoria: String,
    val imagenUrl: String,
    val precio: String,     // Formateado para la UI, ej: $59.990
    val puntuacion: String,  // Ej: "4.5"
    val especificaciones: List<String> = emptyList(),
    val stock: Int = 0,
    val comentarios: List<String> = emptyList()
)
